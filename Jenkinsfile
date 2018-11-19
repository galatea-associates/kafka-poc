pipeline {
    agent any
    environment {
      PATH="/home/ubuntu/.local/bin:$PATH"
    }
    stages {
        stage('Build'){
            steps{
                echo 'hello world'
                sh ''' python3 --version
                       pip3 --version'''
            }
        }
        stage('Static code metrics'){
            steps{
                echo "PEP8 style check"
                sh  ''' pylint3 --disable=C . || true
                    '''
                echo "Code Coverage"
                sh ''' coverage run SimpleProducer.py 1 1 2 3
                       python -m coverage xml -o ./reports/coverage.xml'''
            }
            post{
                always{
                    step([$class: 'CoberturaPublisher',
                                   autoUpdateHealth: false,
                                   autoUpdateStability: false,
                                   coberturaReportFile: 'reports/coverage.xml',
                                   failNoReports: false,
                                   failUnhealthy: false,
                                   failUnstable: false,
                                   maxNumberOfBuilds: 10,
                                   onlyStable: false,
                                   sourceEncoding: 'ASCII',
                                   zoomCoverageChart: false])
                }
            }
        }
    }
}