from DataGenerator import DataGenerator

from googleapiclient.http import MediaIoBaseDownload
from googleapiclient.discovery import build
from httplib2 import Http
from oauth2client import file, client, tools

import io
import os
import time
import argparse
from enum import Enum
from multiprocessing import Lock

SCOPES = 'https://www.googleapis.com/auth/drive'

class FileState(Enum):
    NOT_STARTED = 1
    DOWNLOADING = 2
    DOWNLOADED = 3

class SetActions(Enum):
    START_DOWNLOADING = 1
    WAIT_FOR_DOWNLOAD = 2
    START_PROCESSING = 3

class GoogleDriveAccessor(DataGenerator):
    def __init__(self, folder_id=None, output_folder="out"):
        self.__folder_ID = folder_id
        self.__output_folder = self.__process_path(path=output_folder)
        self.__service = None
        self.__file_download_status = self.__check_download_status()
        self.__lock = Lock()

    def __check_download_status(self):
        #TODO: Implement this download check
        return FileState.NOT_STARTED

    def __auth_gdrive(self):

        store = file.Storage('token.json')
        creds = store.get()
        if not creds or creds.invalid:
            flow = client.flow_from_clientsecrets('credentials.json', SCOPES)
            creds = tools.run_flow(flow, store)
        self.__service = build('drive', 'v3', http=creds.authorize(Http()))

    def __get_files_in_folder(self, folder):
        results = self.__service.files().list(
            q=' "'+folder+'" in parents',fields="files(*)").execute()
        return(results.get('files', []))

    def __download_items(self, items):
        if not items:
            print('No files found.')
        else:
            print('Files:')
            for item in items:
                file_id = item['id']
                file_name = item['name']
                request = self.__service.files().get_media(fileId=file_id)
                fh = io.FileIO(self.__output_folder + file_name, 'w')
                downloader = MediaIoBaseDownload(fh, requests)
                done = False
                while not done:
                    status, done = downloader.next_chunk()
                    print("Download %d%%." % int(status.progress() * 100))

    def __process_path(self, path):
        if not path.endswith(os.path.sep):
            path += os.path.sep
        return path

    def __process_args(self, args):
        if args is None:
            return

        if "Folder ID" in args.keys():
            self.__folder_ID = args["Folder ID"]
        
        if "Output Directory" in args.keys():
            self.__output_folder = self.__process_path(path=args["Output Directory"])

    def __set_downloading_state(self, state):
        self.__file_download_status = state

    def __get_downloading_state(self):
            return self.__file_download_status

    def __download_from_gdrive(self):
        self.__auth_gdrive()

        items = self.__get_files_in_folder(folder=self.__folder_ID)

        self.__download_items(items=items)

    def __start_processing(self):
        pass

    def __get_current_action(self):
        set_action = None
        while set_action in [SetActions.WAIT_FOR_DOWNLOAD, None]:
            with self.__lock:
                set_action = {
                    FileState.DOWNLOADED : SetActions.START_PROCESSING,
                    FileState.DOWNLOADING: SetActions.WAIT_FOR_DOWNLOAD,
                    FileState.NOT_STARTED: SetActions.START_DOWNLOADING
                    }[self.__get_downloading_state()]
                if set_action == SetActions.START_DOWNLOADING:
                    self.__set_downloading_state(FileState.DOWNLOADING)
            time.sleep(1)
        return set_action

    # In DataConfiguration.py, 'Data Args' field should look like:
    # {"Output Directory": "out",
    #  "Folder ID": "2342342341fsdfs342sdf"}
    def run(self, args=None):
        data = None
        self.__process_args(args=args)

        set_action = self.__get_current_action()
        
        if set_action == SetActions.START_DOWNLOADING:
            self.__download_from_gdrive()
            with self.__lock:
                self.__set_downloading_state(FileState.DOWNLOADED)
        
        self.__start_processing()
        
        return data



def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder_id', type=str, required=True, help="")
    parser.add_argument('--output_folder', type=str, default="out", help="")
    return parser.parse_args()

def format_args(output_dir, folder_id):
    return {
        "Output Directory": output_dir,
        "Folder ID": folder_id
    }

if __name__ == "__main__":
    args=get_args()
    formatted_args = format_args(output_dir=args.output_folder, folder_id=args.folder_id)
    GoogleDriveAccessor().run(args=formatted_args)
    