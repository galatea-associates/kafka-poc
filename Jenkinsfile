#!/usr/bin/env groovy

// Based on https://github.com/jerearista/python-jenkinsfile-testing/blob/master/Jenkinsfile
pipeline {
    agent any
    options {
        buildDiscarder(
            // Only keep the 10 most recent builds
            logRotator(numToKeepStr:'10'))
    }
    environment {
        VIRTUAL_ENV = "${env.WORKSPACE}/venv"
    }
    stages {
        stage('Install Requirements'){
            steps {
                sh """
                    echo ${SHELL}
                    [ -d venv ] && rm -rf venv
                    virtualenv venv --python=python3.5
                    #. venv/bin/activate
                    export PATH=${VIRTUAL_ENV}/bin:${PATH}
                    python3 -m pip install -r requirements.txt
                    make clean
                """
            }
        }

        stage ('Check_style') {
            steps {
                sh """
                    #. venv/bin/activate
                    [ -d report ] || mkdir report
                    export PATH=${VIRTUAL_ENV}/bin:${PATH}
                """
                sh """
                    export PATH=${VIRTUAL_ENV}/bin:${PATH}
                    make flake8 | tee report/flake8.log || true
                """
                sh """
                    export PATH=${VIRTUAL_ENV}/bin:${PATH}
                    make pylint | tee report/pylint.log || true
                """
              
            }
        }


        stage ('Docs') {
            steps {
                sh """
                    #. venv/bin/activate
                    export PATH=${VIRTUAL_ENV}/bin:${PATH}
                    PYTHONPATH=. pdoc --html --html-dir docs --overwrite env.projectName
                """
            }

            post {
                always {
                    publishHTML target: [
                        reportDir: 'docs/*',
                        reportFiles: 'index.html',
                        reportName: 'Module Documentation'
                    ]
                }
            }
        }

        stage ('Cleanup') {
            steps {
                sh 'rm -rf venv'
            }
        }
        
    }
} 