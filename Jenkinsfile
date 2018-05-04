#!/usr/bin/env groovy

def botUrl = "http://decidir2bobthebot.marathon.l4lb.thisdcos.directory:8888/notify"
def projectName = "scala-minimal-test"

pipeline {
    agent {
        docker {
            image 'docker.dev.redbee.io/builder:sbt-0.13-jdk-oracle-8'
            args '-v sbt-cache:/root/.sbt/ -v ivy-cache:/root/.ivy2/'
            customWorkspace '/src/'
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Notifying') {
            steps {
                httpRequest(url: "${botUrl}", acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: """
                {
                    "project": "${projectName}",
                    "branch": "${BRANCH_NAME}",
                    "result": "-",
                    "event": "started"
                }
                """)
            }
        }

        stage('Testing & analysing') {
            steps {
                withSonarQubeEnv('Sonar') {
                    sh "sbt clean coverage test coverageReport coverageAggregate"
                }
            }
        }

        stage('Quality gate') {
            when {
                anyOf{
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                sh "sbt sonar"
                script {
                    timeout(time: 1, unit: 'HOURS') {
                        def qg = waitForQualityGate()
                        echo "Found status: ${qg.status}"
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }


        stage('Validate PR'){
            when {
                changeRequest()  // bug: not working
            }
            steps {
                // echo "[DRYRUN] fetch & merge to master"
                echo "Test after merge:"
                sh "sbt clean test"
            }
        }

        stage('New snapshot'){
            when {
                branch "develop"
            }
            steps {
                sh "sbt publishSnapshot"

                // input(message: "Deployar a Desa?")
                // lock('desa-deployment') {
                //     milestone(label: 'desa-deploy')
                //     echo '[DRYRUN] deploy to desa'
                // }
            }
        }

        stage('New release') {
            when {
                branch "master"
            }
            steps {
                timeout(time: 1, unit: 'DAYS') {
                    input(message: 'Con que número de version se hace el release?',
                        ok: 'Build',
                        parameters: [
                            string(defaultValue: ' ',
                            description: 'Version ej: 1.0.0',
                            name: 'RELEASE_VERSION')
                        ]
                    )
                }
                sh "sbt release release-version $RELEASE_VERSION with-defaults"
            }
        }
    }
    post {
        changed {
            // TODO: mail
            echo "[DRYRUN] changed build"
        }
        always {
            httpRequest(url: "${botUrl}", acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: """
            {
                "project": "${projectName}",
                "branch": "${BRANCH_NAME}",
                "result": "${currentBuild.currentResult}",
                "event": "finished"
            }
            """)
        }
    }
}
