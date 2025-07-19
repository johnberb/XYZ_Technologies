pipeline {
    agent any

    environment {
        ANSIBLE_HOME = '/home/ansible/ansible'
        BUILD_NUMBER = "${env.BUILD_ID}"
        REMOTE_ARTIFACT_DIR = '/home/ansible/ansible/tmp/jenkins-artifacts'
    }
    
    stages {
        stage('Compile and Package') {
            steps {
                script {
                    // Trigger compile job and wait for completion
                    def compileBuild = build job: 'CompileXYZ_technologies', wait: true, propagate: true
                    
                    // Find and wait for the downstream Package-job
                    def packageJobBuild = waitForDownstreamBuild('Package-job', compileBuild)
                    
                    // Store the Package-job's workspace path for artifact transfer
                    env.PACKAGE_WORKSPACE = packageJobBuild?.buildVariables?.WORKSPACE ?: '/var/lib/jenkins/workspace/Package-job'
                }
            }
        }
        
        stage('Test SSH Connection') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no -i '$SSH_KEY' root@10.10.10.229 'whoami && pwd && mkdir -p ${REMOTE_ARTIFACT_DIR}'
                        """
                    }
                }
            }
        }
        
        stage('Transfer WAR File') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'Ansible',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: "${env.PACKAGE_WORKSPACE}/target/*.war",
                                    removePrefix: "${env.PACKAGE_WORKSPACE}/target",
                                    remoteDirectory: REMOTE_ARTIFACT_DIR,
                                    execCommand: "chmod 644 ${REMOTE_ARTIFACT_DIR}/*.war"
                                )
                            ],
                            verbose: true
                        )
                    ]
                )
            }
        }
    }
    
    post {
        always {
            cleanWs() 
        }
    }
}

// Helper function to wait for downstream job
def waitForDownstreamBuild(String jobName, upstreamBuild) {
    timeout(time: 30, unit: 'MINUTES') {
        while (true) {
            // Get all downstream builds of the compile job
            def downstreamBuilds = Hudson.instance.getItemByFullName(upstreamBuild.projectName)
                .getBuildByNumber(upstreamBuild.number.toInteger())
                .getDownstreamBuilds()
            
            // Find the specific Package-job
            def packageBuild = downstreamBuilds.find { it.projectName == jobName }
            
            if (packageBuild) {
                echo "Found downstream ${jobName} build #${packageBuild.number}"
                // Wait for it to complete if it's still running
                if (packageBuild.isBuilding()) {
                    echo "Waiting for ${jobName} build #${packageBuild.number} to complete..."
                    sleep 10
                } else {
                    return packageBuild
                }
            } else {
                echo "Waiting for ${jobName} to be triggered..."
                sleep 10
            }
        }
    }
}