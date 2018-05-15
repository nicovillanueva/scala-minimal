#!/usr/bin/env groovy

project = "commons"

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
                script {
                    notify.build(event: "started",
                        project: "${JOB_NAME}",
                        result: "",
                        buildUrl: "${BUILD_URL}")
                }
            }
        }

        stage('New PR opened'){
            when {
                changeRequest()
            }
            steps {
                script {
                    notify.pr(project: "${JOB_NAME}",
                        targetBranch: "${CHANGE_TARGET}",
                        changeId: "${CHANGE_ID}",
                        author: "${CHANGE_AUTHOR}",
                        changeUrl: "${CHANGE_URL}")
                }
            }
        }

        stage('Testing & analysing') {
            steps {
                script {
                    sbt.test()
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
                script {
                    sbt.sonar()
                }
            }
        }

        stage('New snapshot'){
            when {
                branch "develop"
            }
            steps {
                script {
                    sbt.publishSnapshot()
                }
            }
        }

        stage('New release') {
            when {
                branch "master"
            }
            steps {
                script {
                    sbt.release "${project}-release"
                }
            }
        }
    }

    post {
        always {
            script {
                notify.build(event: "finished",
                    project: "${JOB_NAME}",
                    result: "${currentBuild.currentResult != null ? currentBuild.currentResult : "-"}",
                    buildUrl: "${BUILD_URL}")
            }
        }
    }
}
