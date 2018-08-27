def gradle(command) {
    def GRADLE_HOME = tool name: "${env.GRADLE_INSTALL}", type: 'hudson.plugins.gradle.GradleInstallation'
    sh "${GRADLE_HOME}/bin/gradle -Dorg.gradle.warning.mode=none ${command}"
}

pipeline {
    agent any

    parameters {
        choice(choices: 'Alpha\nBeta\nRelease\nDebug', description: 'What type of build?', name: 'buildType')
        string(defaultValue: "develop", description: 'Which Branch?', name: 'branch')
    }

    stages {
        stage('Clone sources') {
            steps {
                dir('code') {
                    git poll: true, url: 'git@github.com:septadev/SEPTA-Android.git', branch: params.branch, credentialsId: env.SEPTA_KEY_CREDENTIALS_ID
                }
            }

        }

        stage('Get Database') {
            steps {
                script {
                    def props = readProperties file: 'code/app/src/main/assets/databases/database_version.properties'
                    def codeVersionNum = props['databaseVersion'].toInteger()
                    def filename = "${props['databaseFileName']}.zip"

                    echo "code version: ${codeVersionNum}, ${filename}"

                    def tmpDir = pwd tmp: true
                    def jsonFileLocation = "latestDb.json"
                    httpRequest url: "https://s3.amazonaws.com/mobiledb.septa.org/latest/latestDb.json", outputFile: jsonFileLocation
                    def data = readJSON file: jsonFileLocation
                    def currentVersion = data.version
                    def dbUrl = data.url
                    echo "current version: ${currentVersion}, ${dbUrl}"

                    if (codeVersionNum < currentVersion) {
                        sh 'rm code/app/src/main/assets/databases/*.zip'
                        httpRequest url: dbUrl, outputFile: "code/app/src/main/assets/databases/SEPTA_${currentVersion}.sqlite.zip"
                        def output = "databaseVersion=${currentVersion}\ndatabaseFileName=SEPTA_${currentVersion}.sqlite"
                        writeFile file: 'code/app/src/main/assets/databases/database_version.properties', text: output
                        dir('code') {
                            sshagent(credentials: ["${env.SEPTA_KEY_CREDENTIALS_ID}"]) {
                                sh "git add app/src/main/assets/databases/SEPTA_${currentVersion}.sqlite.zip"
                                sh "git commit -a -m \"Updated embeded database to version ${currentVersion}.\""
                                sh 'git push '
                            }
                        }
                    }
                }
            }
        }

        stage('Gradle build') {
            steps {
                dir('code') {
                    script {
                        def command = "assemble${params.buildType}"
                        gradle command
                    }
                }
            }
        }

        stage('Publish build') {
            steps {
                script {
                    def buildTypeLowerCase = params.buildType.toLowerCase()
                    sh "aws s3 cp code/app/build/outputs/apk/${buildTypeLowerCase}/app-${buildTypeLowerCase}.apk s3://mobile-dev-distribution/septa-app_${env.BUILD_NUMBER}.apk --acl public-read"
                }
            }
        }

        stage('Notification') {
            steps {
                script {
                    def git_log = "<table border=\"1\"><tr><td>Date</td><td>Author</td><td>Commit Message</td>"

                    sh "cp ${env.LAST_SUCCESS_COMMIT_FILE} last_success_commit.txt"
                    def last_success_commit = readFile 'last_success_commit.txt'
                    last_success_commit = last_success_commit.trim()
                    echo last_success_commit
                    dir('code') {
                        def cmd = "git log --pretty=format:'<tr><td>%ad</td><td>%an</td><td>%s</td></tr>' --date=short  ${last_success_commit}..HEAD"
                        echo cmd
                        git_log += sh script: cmd, returnStdout: true
                        echo git_log
                    }


                    dir('code') {
                        sh "git rev-parse HEAD > ${env.LAST_SUCCESS_COMMIT_FILE}"
                    }

                    sh 'rm last_success_commit.txt'
                    git_log += "</table>"

                    accessUrl = "https://s3.amazonaws.com/mobile-dev-distribution/septa-app_${env.BUILD_NUMBER}.apk"
                    withEnv(["accessUrl=${accessUrl}", "git_log=${git_log}"]) {
                        body = sh script: 'code/devops/distribution_email_body.sh', returnStdout: true
                    }

                    distroText = readFile 'code/devops/email_distro.txt'
                    distroAddresses = distroText.split("\n").join(',')
                    emailext(
                            mimeType: 'text/html',
                            subject: "New SEPTA Android APK Available SUCCESSFUL [${env.BUILD_NUMBER}]'",
                            body: body,
                            to: distroAddresses
                    )
                }
            }
        }
    }

    post {
        failure {
            script {
                if (currentBuild.currentResult == 'FAILURE') { // Other values: SUCCESS, UNSTABLE
                    // Send an email only if the build status has changed from green/unstable to red
                    emailext subject: '$DEFAULT_SUBJECT',
                            body: '$DEFAULT_CONTENT',
                            to: "${env.FAILURE_DISTRO_LIST}"
                }
            }
        }
    }

}