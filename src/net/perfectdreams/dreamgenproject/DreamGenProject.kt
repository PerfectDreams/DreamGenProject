package net.perfectdreams.dreamgenproject

import java.io.File

object DreamGenProject {
	@JvmStatic
	fun main(args: Array<String>) {
		// Ao usar File(".") e rodar ele pelo IntelliJ IDEA, ele retorna a pasta do projeto
		val rootFolder = File(".")
		// Por isto nós iremos pegar a pasta aonde tem as "coisas interessantes"
		val assets = File(rootFolder, "DreamGenProject/assets")

		println("Bukkit ou Bungee?")
		val platform= readLine()!!.toLowerCase()

		val folderName = if (platform == "bungee") {
			"DreamBasePluginBungee"
		} else {
			"DreamBasePlugin"
		}

		println("Nome do projeto:")
		val projectName= readLine()

		println("Criando projeto $projectName...")

		val template = File(assets, folderName)
		val projectFolder = File(rootFolder, projectName)
		template.copyRecursively(projectFolder)

		// Copiar todas as cosias "misc" para o nosso projeto
		File(assets, "misc").listFiles().forEach {
			it.copyTo(File(projectFolder, it.name))
		}

		// E agora vamos alterar todos os templates
		recursiveFileReplace(projectName!!, projectFolder)

		// E alterar alguns nomes de alguns arquivos
		File(projectFolder, "src\\main\\kotlin\\net\\perfectdreams\\dreambaseplugin\\DreamBasePlugin.kt")
				.renameTo(File(projectFolder, "src\\main\\kotlin\\net\\perfectdreams\\dreambaseplugin\\$projectName.kt"))

		File(projectFolder, "src\\main\\kotlin\\net\\perfectdreams\\dreambaseplugin")
				.renameTo(File(projectFolder, "src\\main\\kotlin\\net\\perfectdreams\\${projectName.toLowerCase()}"))

		File(rootFolder, "$projectName\\DreamBasePlugin.iml")
				.renameTo(File(rootFolder, "$projectName\\$projectName.iml"))

		// Criar o build do projeto
		File(assets, "${folderName}_jar.xml")
				.copyTo(File(rootFolder, ".idea\\artifacts\\${projectName}_jar.xml"))

		val buildFile = File(rootFolder, ".idea\\artifacts\\${projectName}_jar.xml")

		val replacedLines = mutableListOf<String>()

		for (line in buildFile.readLines()) {
			var line = line.replace("{projectName}", projectName)
			line = line.replace("{lProjectName}", projectName.toLowerCase())
			replacedLines.add(line)
		}

		buildFile.writeText(replacedLines.joinToString("\n"))

		// Adicionar module na workspace
		val workspaceFile = File(rootFolder, ".idea\\modules.xml")

		val injectedModuleLines = mutableListOf<String>()

		var idx = 0;
		for (line in workspaceFile.readLines()) {
			if (line == "    </modules>") {
				injectedModuleLines.add("      <module fileurl=\"file://\$PROJECT_DIR\$/$projectName/$projectName.iml\" filepath=\"\$PROJECT_DIR\$/$projectName/$projectName.iml\" />")
			}
			injectedModuleLines.add(line)
			idx++;
		}

		workspaceFile.writeText(injectedModuleLines.joinToString("\n"))

		println("Projeto criado com sucesso!")
	}

	fun recursiveFileReplace(projectName: String, projectFolder: File) {
		projectFolder.listFiles().forEach {
			if (it.isDirectory) {
				recursiveFileReplace(projectName, it)
			} else {
				val replacedLines = mutableListOf<String>()

				for (line in it.readLines()) {
					var line = line.replace("{projectName}", projectName)
					line = line.replace("{lProjectName}", projectName.toLowerCase())
					replacedLines.add(line)
				}

				it.writeText(replacedLines.joinToString("\n"))
			}
		}
	}
}