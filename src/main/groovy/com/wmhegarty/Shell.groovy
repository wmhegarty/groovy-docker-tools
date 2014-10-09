package com.wmhegarty

// run shell commands and output line bt line/\. Standard grooovy exec will not return till all lines are complete
public class Shell {
  public static def execute(String command, boolean showOutput = true) {
    def process = new ProcessBuilder(addShellPrefix(command))
                                      .redirectErrorStream(true) 
                                      .start()
    process.inputStream.eachLine { if(showOutput) println it }
    process.waitFor();
    return process.exitValue()
  }
  public static def executeReturnOutput(String command) {
    def process = new ProcessBuilder(addShellPrefix(command))
                                      .redirectErrorStream(true) 
                                      .start()
    def result = ''                                      
    process.inputStream.eachLine { result += "${it}\n" }
    process.waitFor();
    return result.trim()
  }
   
  private static def addShellPrefix(String command) {
    def commandArray = new String[3]
    commandArray[0] = "sh"
    commandArray[1] = "-c"
    commandArray[2] = command
    return commandArray
  }
}