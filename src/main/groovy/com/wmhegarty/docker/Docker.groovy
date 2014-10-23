package com.wmhegarty.docker

import com.wmhegarty.Shell
import groovyx.net.http.RESTClient
import java.net.*
import static groovyx.net.http.ContentType.*


public class Docker {
    static restClient = new RESTClient('http://localhost:4243')

    public Docker(){
        restClient = new RESTClient('http://localhost:4243')
    }
    
    public Docker(ipAddress){
        restClient = new RESTClient("http://${ipAddress}:4243")
    }

    public getRunningContainerNames() {
        def response = restClient.get(path: '/containers/json')
        def names = response.data.collect() {
            it['Names'][0].minus('/')
        };
        return names
    }

    public stopContainers(containerNames) {
        containerNames.each() {
            stopContainer(it)
        }
    }

    public stopContainer(name) {
        Shell.execute("docker stop ${name}")
    }

    public removeContainers(containerNames) {
        containerNames.each() {
            removeContainer(it)
        }
    }

    public removeContainer(name) {
        Shell.execute("docker rm ${name}")
    }

    public getImageNames() {
        def response = restClient.get(path: '/images/json')
        response.data.collect {
            [id: it['Id'], tags: it['RepoTags']]
        }
    }

    public build(name, version, dockerDirectory) {
        def result = Shell.execute("docker build -t ${name}:${version} ${dockerDirectory}")
        if (result != 0) {
            throw new Exception("Docker build failed")
        }

    }

    public runImage(name, version, options) {
        def image = "${name}:${version}"
        Shell.execute("docker run${convertOptionsToArgs(options)} -d ${image}")
    }

    public getHostDockerIp() {
        def network = NetworkInterface.getByName('docker0')
        def result = ''
        network.getInetAddresses().each {
            if (it.getClass() == java.net.Inet4Address) {
                result = it.getHostAddress()
            }
        }
        result
    }

    public getContainerIp(container) {
        return Shell.executeReturnOutput("""docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container}""")
    }

    public String saveImage(String imageName, String imageTag, String directory) {
        Shell.execute("docker save ${imageName}:${imageTag} > ${directory}/${imageName}-${imageTag}-image.tar")
        Shell.execute("pbzip2 -f ${directory}/${imageName}-${imageTag}-image.tar")
        return " ${directory}/${imageName}-${imageTag}-image.tar.bz2"
    }

    public loadImage(String imageName, String imageTag, String directory) {
        Shell.execute("pbzip2 -d ${directory}/${imageName}-${imageTag}-image.tar.bz2")
        Shell.execute("docker load < ${directory}/${imageName}-${imageTag}-image.tar")
        Shell.execute("docker images | grep \\<none\\> | awk '{print \$3}'|  xargs -I hash docker tag hash  ${imageName}:${imageTag}")
    }

    public deleteImage(String imageName, String imageTag) {
        Shell.execute("docker rmi ${imageName}:${imageTag}")
    }

    public exportContainer(String containerName) {
        Shell.execute("docker export ${containerName} > ${containerName}.tar")
    }

    public importContainer(String containerName) {
        Shell.execute("docker import - ${containerName} < ${containerName}.tar")
    }

    private convertOptionsToArgs(options) {
        def result = ''
        options.each {
            switch (it.key) {
                case "ports":
                    it.value.each { result += " -p ${it}" }
                    break
                case "port":
                    result += " -p ${it.value}"
                    break
                case "name":
                    result += " --name=${it.value}"
                    break
                case "volumes":
                    it.value.each { result += " -v ${it}" }
                    break
                case "volume":
                    result += " -v ${it.value}"
                    break
                case "dns":
                    result += " --dns=${it.value}"
                    break
                case "env":
                    result += " -e ${it.value}"
                    break
                case "env_vars":
                    it.value.each { result += " -e ${it}" }
                    break
                case "link":
                    result += " --link ${it.value}"
                    break; 
                case "links":
                    it.value.each { result += " --link ${it}" }
                    break;         
                case "volumesFrom":
                    it.value.each { result += " --volumes-from ${it}" }    
                    break;    
                default:
                    result += " ${it.key} ${it.value}"
            }

        }
        return result
    }

}