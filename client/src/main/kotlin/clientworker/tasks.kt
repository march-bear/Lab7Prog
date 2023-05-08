package clientworker

import command.ArgumentType
import command.ArgumentValidator
import command.CommandArgument
import exceptions.ScriptException
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import request.Request
import java.lang.Exception
import java.util.*

val startTasks = module {
    factory(named("checkConnect")) {
        Task {
            Messenger.print("Проверка соединения...")
            val response = sendAndReceive(Request("check", "CHECK", user = user, passwd = token ?: password))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)
            Messenger.print(response.message)
            for (task in response.necessaryTask!!.split(' '))
                get<Task>(named(task)).execute(this)
        }
    }

    factory(named("getCommandInfo")) {
        Task {
            Messenger.print("Запрос на получение актуального списка команд...")

            val response = sendAndReceive(Request("help", "HELP", user = user, passwd = token ?: password))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)

            Messenger.print("Ответ получен", TextColor.BLUE)

            if (response.requestKey == "HELP") {
                if (response.success) {
                    try {
                        updateCommandList(Json.decodeFromString(response.message))
                    } catch (ex: SerializationException) {

                    } catch (ex: Exception) {

                    }
                    if (response.necessaryTask != null) get<Task>(named(response.necessaryTask!!)).execute(this)
                } else {

                    if (response.necessaryTask != null) get<Task>(named(response.necessaryTask!!)).execute(this)
                }
            } else {
                Messenger.print("Ответ сервера некорректен, попытка повторной отправки запроса")
                Thread.sleep(500)
                get<Task>(named("getCommandInfo")).execute(this)
            }
        }
    }

    factory(named("logIn")) {
        Task {

        }
    }

    factory(named("register")) {
        Task {

        }
    }

    factory(named("identify")) {
        Task {
            val r = Reader()
            Messenger.print("Требуется вход.", TextColor.RED)
            Messenger.inputPrompt("Введите вариант подключения к серверу (1 - авторизоваться, 2 - зарегистрироваться)")
            var v: String
            while (true) {
                v = r.readString()
                when (v) {
                    "1" -> { get<Task>(named("logIn")).execute(this); break }
                    "2" -> { get<Task>(named("register")).execute(this); break }
                    else -> Messenger.print("Повторите ввод: ", TextColor.RED, false)
                }
            }
        }
    }
}

val executeCommandTasks = module {
    factory(named("help")) {
            (args: CommandArgument) ->
        Task {
            ArgumentValidator(listOf()).check(args)

            Messenger.print(String.format("%-40s", "help:"), newLine = false)
            Messenger.print("вывести список доступных команд", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "exit:"), newLine = false)
            Messenger.print("завершить работу клиента", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "execute_script:"), newLine = false)
            Messenger.print("исполнить скрипт", TextColor.BLUE)

            for (commandInfo in getCommandList()) {
                Messenger.print(String.format("%-40s", "${commandInfo.name}:"), newLine = false)
                Messenger.print(commandInfo.info, TextColor.BLUE)
            }
        }
    }

    factory(named("exit")) {
            (args: CommandArgument) ->
        Task {
            ArgumentValidator(listOf()).check(args)
        }
    }

    factory(named("execute_script")) {
            (args: CommandArgument) ->
        Task {
            ArgumentValidator(listOf(ArgumentType.STRING)).check(args)
            val requestList: LinkedList<Pair<String, CommandArgument>> = LinkedList()
            val fileName: String = args.primArgs[0]
            val scriptFiles = LinkedList<String>()

            try {
                addCommandsFromFile(fileName, requestList, scriptFiles, getCommandList())
            } catch (ex: ScriptException) {
                Messenger.print(ex.message, TextColor.RED)
                return@Task
            }

            for (command in requestList) {
                when (command.first) {
                    "exit", "help", "execute_script" ->
                        get<Task>(named(command.first)) { parametersOf(command.second) }.execute(this)
                    else -> {
                        val key = ChannelClientWorker.generateKey()
                        val response = sendAndReceive(Request(command.first, key, command.second, user, token ?: password))
                    }
                }
            }
        }
    }
}

