import argparse
import csv
import os
from functools import partial
from Runnable import Runnable
from DataGenerator import DataGenerator

ddc = DataGenerator()
data_template = {
    'inst-ref': {
        'inst_id': {'func': ddc.generate_new_inst_id, 'args': ['asset_class']},
        'ric': {'func': ddc.generate_ric, 'args': ['ticker', 'asset_class']},
        'isin': {'func': ddc.generate_isin, 'args': ['coi', 'cusip', 'asset_class']},
        'sedol': {'func': ddc.generate_sedol, 'args': ['ticker', 'asset_class']},
        'ticker': {'func': ddc.generate_ticker, 'args': ['asset_class']},
        'cusip': {'func': ddc.generate_cusip, 'args': ['ticker', 'asset_class']},
        'asset_class': {'func': ddc.generate_asset_class},
        'coi': {'func': ddc.generate_coi}
    },
    'price': {
        'inst_id': {'func': ddc.generate_inst_id, 'args': ['asset_class']},
        'price': {'func': ddc.generate_price, 'args': ['inst_id']},
        'curr': {'func': ddc.generate_currency},
    },
    'front_office_position': {
        'inst_id': {'func': ddc.generate_inst_id, 'args': ['asset_class']},
        'type': {'func': ddc.generate_type},
        'knowledge_date': {'func': ddc.generate_knowledge_date},
        'effective_date': {'func': partial(ddc.generate_effective_date, n_days_to_add=3), 'args': ['knowledge_date']},
        'account': {'func': ddc.generate_account},
        'direction': {'func': ddc.generate_direction},
        'qty': {'func': ddc.generate_qty},
        'purpose': {'func': partial(ddc.generate_purpose, data_type='FOP')},
    },
    'depot_position': {
        'inst_id': {'func': ddc.generate_inst_id, 'args': ['asset_class']},
        'type': {'func': ddc.generate_type},
        'knowledge_date': {'func': ddc.generate_knowledge_date},
        'effective_date': {'func': ddc.generate_effective_date, 'args': ['knowledge_date']},
        'account': {'func': ddc.generate_account},
        'direction': {'func': ddc.generate_direction},
        'qty': {'func': ddc.generate_qty},
        'purpose': {'func': partial(ddc.generate_purpose, data_type='DP')},
        'depot_id': {'func': ddc.generate_depot_id}
    },
    'order_execution': {
        'order_id': {'func': ddc.generate_order_id, 'args': ['asset_class']},
        'customer_id': {'func': ddc.generate_customer_id, 'args': ['asset_class']},
        'direction': {'func': ddc.generate_direction},
        'sto_id': {'func': ddc.generate_sto_id, 'args': ['asset_class']},
        'agent_id': {'func': ddc.generate_agent_id, 'args': ['asset_class']},
        'price': {'func': ddc.generate_price, 'args': ['inst_id']},
        'inst_id': {'func': ddc.generate_inst_id, 'args': ['asset_class']},
        'qty': {'func': ddc.generate_qty}
    },
    'stock_loan': {
        'inst_id': {'func': partial(ddc.generate_inst_id, only='S'), 'args': ['asset_class']},
        'knowledge_date': {'func': ddc.generate_knowledge_date},
        'effective_date': {'func': ddc.generate_effective_date, 'args': ['knowledge_date']},
        'purpose': {'func': partial(ddc.generate_purpose, data_type='SL')},
        'qty': {'func': ddc.generate_qty},
        'collateral_type': {'func': ddc.generate_collateral_type},
        'haircut': {'func': ddc.generate_haircut},
        'rebate_rate': {'func': ddc.generate_rebate_rate, 'args': ['collateral_type']},
        'termination_date': {'func': ddc.generate_termination_date},
        'account': {'func': ddc.generate_account},
        'is_callable': {'func': ddc.generate_is_callable}
    }
}

class DictRunnable(Runnable):

    # In DataConfiguration.py, 'Data Args' field should look like:
    # {'Type': 'position'}
    def run(self, args):
        pass

    @staticmethod
    def main():
        args = get_args()
        dict_data_generator = DictRunnable()
        dict_data_generator .__generate_data_files(args)

    def __generate_data_files(self, args):
        # Create out directory if it does not yet exist
        if not os.path.exists('out'):
            os.makedirs('out')
        if args.inst_refs > 0:
            self.__create_data_file('out/inst-refs.csv', args.inst_refs, 'inst-ref')
        if args.prices > 0:
            self.__create_data_file('out/prices.csv', args.prices, 'price')
        if args.front_office_positions > 0:
            self.__create_data_file('out/front_office_positions.csv', args.front_office_positions, 'front_office_position')
        if args.depot_positions > 0:
            self.__create_data_file('out/depot_positions.csv', args.depot_positions, 'depot_position')
        if args.order_executions > 0:
            self.__create_data_file('out/order_executions.csv', args.order_executions, 'order_execution')

    # file_name corresponds to the name of the CSV file the function will write to
    # n is the number of data entities to write to the CSV file
    # data_generator is the function reference that generates the data entity of interest
    def __create_data_file(self, file_name, n, data_type):
        # w+ means create file first if it does not already exist
        with open(file_name, mode='w+', newline='') as file:
            data = self.__generate_data(data_template[data_type])
            writer = csv.DictWriter(file, fieldnames=list(data))
            writer.writeheader()
            writer.writerow(data)
            # n - 1 because we already wrote to the file once with the entity variable
            # We do this to get the keys of the dictionary in order to get the field names of the CSV file
            for _ in range(n - 1):
                entity = self.__generate_data(data_template[data_type])
                writer.writerow(entity)

    def __generate_data(self, template):
        data = {}
        for field_to_generate, generator_function in template.items():
            if field_to_generate not in data:
                if ddc.state_contains_field(field_to_generate):
                    data[field_to_generate] = ddc.get_state_value(field_to_generate)
                elif 'args' in generator_function:
                    args = {}
                    for arg in generator_function['args']:
                        if arg in data:
                            args[arg] = data[arg]
                        elif ddc.state_contains_field(arg):
                            args[arg] = ddc.get_state_value(arg)
                    data[field_to_generate] = generator_function['func'](**args)
                else:
                    data[field_to_generate] = generator_function['func']()
        ddc.clear_state()

        return data


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--prices', nargs='?', type=int, default=0)
    parser.add_argument('--front-office-positions', nargs='?', type=int, default=0)
    parser.add_argument('--inst-refs', nargs='?', type=int, default=0)
    parser.add_argument('--depot-positions', nargs='?', type=int, default=0)
    parser.add_argument('--order-executions', nargs='?', type=int, default=0)
    return parser.parse_args()

if __name__ == '__main__':
    DictRunnable.main()
