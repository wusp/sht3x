node(){
    withCredentials([usernamePassword(credentialsId: 'crowd', passwordVariable: 'ARTIFACTORY_PASS', usernameVariable: 'ARTIFACTORY_USER')]){

        stage('Preparing'){
            echo "Building ${env.BRANCH_NAME}"
            echo "env ${env}"
            checkout scm
        }

        stage('Building'){

            sh' ./gradlew clean model assembleRelease'
        }

        stage('Testing'){

        }

        stage('Archive'){
            // sh' ./gradlew uploadArchives'
            def server = Artifactory.newServer url: 'http://artifactory.segmetics.com/artifactory' , username: env.ARTIFACTORY_USER, password: env.ARTIFACTORY_PASS
            def buildInfo = Artifactory.newBuildInfo()
            buildInfo.env.capture = true
            buildInfo.env.collect()

            def folder = 'Release'
            if (env.BRANCH_NAME != 'master') {
                folder = "Snapshot/${env.BRANCH_NAME}"
            }
            def uploadSpec = """{
              "files": [
                {
                  "pattern": "app/build/outputs/apk/*.apk",
                  "target": "apk/com/maxtropy/ilaundry-box-app/${folder}/"
                }
              ]
            }"""
            server.upload spec: uploadSpec, buildInfo: buildInfo

            buildInfo.retention maxBuilds: 10, maxDays: 7, deleteBuildArtifacts: true
            server.publishBuildInfo buildInfo
        }
    }
}