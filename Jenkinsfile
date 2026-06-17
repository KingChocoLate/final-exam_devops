pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        APP_NAME = 'idcard'
        GIT_BRANCH = 'main'
        ANSIBLE_INVENTORY = 'ansible/inventory.ini'
        ANSIBLE_PLAYBOOK = 'ansible/q3-playbook.yml'
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                git branch: "${GIT_BRANCH}",
                    url: 'https://github.com/KingChocoLate/final-exam_devops.git'
            }
        }

        stage('Build With Maven') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Run Tests With SQLite') {
            steps {
                sh './mvnw test -Dspring.profiles.active=test'
            }
        }

        stage('Deploy With Ansible') {
            steps {
                sh 'ansible-playbook -i ${ANSIBLE_INVENTORY} ${ANSIBLE_PLAYBOOK}'
            }
        }
    }

    post {
        success {
            echo 'Build, test, and Ansible deployment completed successfully.'
        }

        failure {
            script {
                def commitEmail = sh(
                    script: "git log -1 --pretty=format:'%ae'",
                    returnStdout: true
                ).trim()

                mail(
                    to: "${commitEmail}",
                    cc: 'srengty@gmail.com',
                    subject: "Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
Hello,

The Jenkins pipeline failed.

Project: ${APP_NAME}
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Please check the Jenkins console output for details.

Regards,
Jenkins
"""
                )
            }
        }
    }
}