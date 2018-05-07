#!/usr/bin/env groovy

botUrl = "http://decidir2bobthebot.marathon.l4lb.thisdcos.directory:8888/notify" // Global

def notifyBuild(String event, String result = null) {
    httpRequest(url: "${botUrl}/build", contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: """
    {
        "project": "${JOB_NAME}",
        "result": "${result != null ? result : "-"}",
        "phase": "${event}",
        "build_url": "${BUILD_URL}"
    }
    """)
}

def notifyPr() {
    httpRequest(url: "${botUrl}/pr", contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: """
    {
        "project": "${JOB_NAME}",
        "target": "${CHANGE_TARGET}",
        "changeId": "${CHANGE_ID}",
        "author": "${CHANGE_AUTHOR}",
        "changeUrl": "${CHANGE_URL}"
    }
    """)
}

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
                notifyBuild "started"
            }
        }

        stage('New PR opened'){
            when {
                changeRequest()
            }
            steps {
                notifyPr()
            }
        }

        stage('Testing & analysing') {
            steps {
                withSonarQubeEnv('Sonar') {
                    ansiColor('xterm') {
                        sh "sbt clean coverage test coverageReport coverageAggregate"
                    }
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
                withSonarQubeEnv('Sonar') {
                    ansiColor('xterm') {
                        sh "sbt sonar"
                    }
                }
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

        stage('New snapshot'){
            when {
                branch "develop"
            }
            steps {
                ansiColor('xterm') {
                    sh "sbt publishSnapshot"
                }
            }
        }

        stage('New release') {
            when {
                anyOf {
                    branch "master"
                    branch "release/*"
                }
            }
            steps {
                lock(resource: 'commons-release', inversePrecedence: true) {
                    timeout(time: 1, unit: 'DAYS') {
                        notifyBuild "waiting"
                        input(message: 'Con que número de version se hace el release?',
                            ok: 'Build',
                            parameters: [
                                string(defaultValue: ' ',
                                description: 'Version ej: 1.0.0',
                                name: 'RELEASE_VERSION')
                            ]
                        )
                        milestone label: 'commons-release', ordinal: 1
                    }
                }
                ansiColor('xterm') {
                    sh "sbt release release-version ${RELEASE_VERSION} with-defaults"
                }
            }
        }
    }

    post {
        always {
            // notifyBuild "finished" "${currentBuild.currentResult}"

            httpRequest(url: "${botUrl}/build", contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: """
            {
                "project": "${JOB_NAME}",
                "result": "${currentBuild.currentResult != null ? currentBuild.currentResult : "-"}",
                "phase": "finished",
                "build_url": "${BUILD_URL}"
            }
            """)
        }
    }
}
