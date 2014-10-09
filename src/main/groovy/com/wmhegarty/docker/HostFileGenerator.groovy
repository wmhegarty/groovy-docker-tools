package com.wmhegarty.docker

public class HostFileGenerator {
	private def docker = new Docker()
	private def map = [:]

	public def generate(domain,additionalEntries = [:]){
		makeMap()
		def result = map.collect {k,v -> "${v} ${k} ${k}.${domain}"}.join('\n') + '\n'
		if(additionalEntries != null)
			result += additionalEntries.collect {k,v -> "${v} ${k} ${k}.${domain}"}.join('\n')
		result	
	}

	private makeMap()
	{
		def containers = docker.getRunningContainerNames()
		map = [:]	  
		containers.each {
			map[it] = docker.getContainerIp(it)
		}
	}

}
