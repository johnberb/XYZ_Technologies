pipeline {
    agent none 

    environment {
        ANSIBLE_HOME = '/home/ansible/ansible'
        REMOTE_ARTIFACT_DIR = '/home/ansible/ansible/tmp/jenkins-artifacts'
        WAR_SOURCE_PATH = '/var/lib/jenkins/workspace/Package-job/target/XYZtechnologies-1.0.war'
    }
    
    stages {
        stage('Compile and Package') {
            agent { label 'master || Built-In Node' } 
            steps {
                build job: 'CompileXYZ_technologies', wait: true, propagate: true
                build job: 'Package-job', wait: true, propagate: true
            }
        }
        
        stage('Verify Files') {
            agent { label 'master' } 
            steps {
                script {
                    def exists = fileExists "${env.WAR_SOURCE_PATH}"
                    if (!exists) {
                        sh "ls -la /var/lib/jenkins/workspace/Package-job/target/"
                        error "WAR file not found at ${env.WAR_SOURCE_PATH}"
                    }
                }
            }
        }
        
        stage('Transfer WAR File') {
            agent { label 'JenkinsNode' } 
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no -i '$SSH_KEY' root@10.10.10.229 \
                                'mkdir -p ${REMOTE_ARTIFACT_DIR}'
                            scp -i '$SSH_KEY' ${env.WAR_SOURCE_PATH} \
                                root@10.10.10.229:${REMOTE_ARTIFACT_DIR}/
                            ssh -i '$SSH_KEY' root@10.10.10.229 \
                                'ls -la ${REMOTE_ARTIFACT_DIR} && chmod 644 ${REMOTE_ARTIFACT_DIR}/*.war'
                        """
                    }
                }
            }
        }

        stage('Run Ansible Playbook') {
            agent { label 'JenkinsNode' } 
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'Ans2-ssh-key',
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh '''
                        # Copy all necessary files
                        scp -i "$SSH_KEY" \
                            "/var/lib/jenkins/workspace/BuildingXYZTechnologies/Dockerfile.j2" \
                            ansible@10.10.10.229:"/home/ansible/ansible/files/"
                        scp -i "$SSH_KEY" \
                            "/var/lib/jenkins/workspace/BuildingXYZTechnologies/DockerBuildXYZ.yml" \
                            ansible@10.10.10.229:"/home/ansible/ansible/playbooks/"
                        scp -i "$SSH_KEY" \
                            "/var/lib/jenkins/workspace/BuildingXYZTechnologies/KudeDeployXYZ.yml" \
                            ansible@10.10.10.229:"/home/ansible/ansible/playbooks/"
                        scp -i "$SSH_KEY" \
                            "/var/lib/jenkins/workspace/BuildingXYZTechnologies/monitoringDeployed.yml" \
                            ansible@10.10.10.229:"/home/ansible/ansible/playbooks/"
                        
                        # Copy and secure SSH key
                        scp -i "$SSH_KEY" "$SSH_KEY" ansible@10.10.10.229:/home/ansible/.ssh/jenkins_key
                        ssh -i "$SSH_KEY" ansible@10.10.10.229 "chmod 600 ~/.ssh/jenkins_key"
  
                        # Execute Ansible playbook
                        ssh -i "$SSH_KEY" ansible@10.10.10.229 "
                            cd /home/ansible/ansible &&
                            ansible-playbook \
                                -i /etc/ansible/hosts \
                                playbooks/DockerBuildXYZ.yml \
                                --extra-vars 'artifact_path=/tmp/jenkins-artifacts/XYZtechnologies-1.0.war'
                        "
                    '''
                }
            }
        }

        stage('Deploy to K8s') {
            agent { label 'JenkinsNode' } 
            steps {
                script {
                    withCredentials([sshUserPrivateKey(       
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        sh """
                            ssh -i "$SSH_KEY" ansible@10.10.10.229 "
                                cd ${ANSIBLE_HOME} && \
                                ansible-playbook \
                                    -i /etc/ansible/hosts \
                                    playbooks/KudeDeployXYZ.yml \
                                    --extra-vars \\"image_tag=${BUILD_NUMBER}\\"
                            "
                        """
                    }
                }
            }
        }

        stage('Monitor Deployment') {
            agent { label 'JenkinsNode' } 
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        sh """
                            ssh -i "$SSH_KEY" ansible@10.10.10.229 "
                                cd /home/ansible/ansible &&
                                ansible-playbook \
                                    -i /etc/ansible/hosts \
                                    playbooks/monitoringDeployed.yml \
                                    --extra-vars 'image_tag=${BUILD_NUMBER}'
                            "
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Build completed with status: ${currentBuild.result}"
            }
        }
    }
}
