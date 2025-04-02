pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        EC2_USER = 'ec2-user'
        EC2_HOST = '3.253.99.54'
        BACKEND_IMAGE = "kyoka74022/ecommerce-backend:${BUILD_NUMBER}"
        BACKEND_CONTAINER = "ecommerce-backend"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven Project') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t ${BACKEND_IMAGE} ."
            }
        }

        stage('Login to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKERHUB_PSW', usernameVariable: 'DOCKERHUB_USR')]) {
                    bat 'echo %DOCKERHUB_PSW%| docker login -u %DOCKERHUB_USR% --password-stdin'
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                bat "docker push ${BACKEND_IMAGE}"
            }
        }

        stage('Deploy to EC2') {
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKERHUB_PSW', usernameVariable: 'DOCKERHUB_USR')]) {
                        powershell '''
                            $keyPath = $env:SSH_KEY
                            $dockerUser = $env:DOCKERHUB_USR
                            $dockerPass = $env:DOCKERHUB_PSW

                            # Create a simple SSH command to run the Docker commands
                            $sshCommand = "ssh -i `"$keyPath`" -o StrictHostKeyChecking=no ${env:EC2_USER}@${env:EC2_HOST} 'echo $dockerPass | sudo docker login -u $dockerUser --password-stdin && sudo docker pull ${env:BACKEND_IMAGE} && sudo docker stop ${env:BACKEND_CONTAINER} || true && sudo docker rm ${env:BACKEND_CONTAINER} || true && sudo docker run -d -p 8080:8080 --name ${env:BACKEND_CONTAINER} ${env:BACKEND_IMAGE}'"

                            # Execute the SSH command
                            Invoke-Expression $sshCommand

                            # Check if the command was successful
                            if ($LASTEXITCODE -ne 0) {
                                Write-Error "SSH command failed with exit code: $LASTEXITCODE"
                                exit 1
                            }
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            bat 'docker logout'
        }
        success {
            echo 'E-commerce backend deployment successful!'
        }
        failure {
            echo 'E-commerce backend deployment failed!'
        }
    }
}