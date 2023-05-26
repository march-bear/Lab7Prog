package clientworker

import Task
import clientworker.ChannelClientWorker.Companion.generateKey
import command.ArgumentType
import command.ArgumentValidator
import command.CommandArgument
import exceptions.ScriptException
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import iostreamers.readNotEmptyString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import message.Request
import message.Response
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.system.exitProcess

val startTasks = module {
    factory(named("checkConnect")) {
        Task<ChannelClientWorker> {
            val key = generateKey()
            val args = CommandArgument()
            args.setToken(token)
            val response = sendAndReceive(Request(key, "check_connect", args))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)
            println("Ответ получен")
            processMessage(response, key)
        }
    }

    factory(named("getCommandInfo")) {
        Task<ChannelClientWorker> {
            Messenger.print("Запрос на получение актуального списка команд")

            for (i in 1..5) {
                Messenger.print("Попытка отправки запроса...")
                val key = generateKey()
                val args = CommandArgument()
                args.setToken(token)
                val response = sendAndReceive(Request(key, "help", args))
                    ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)

                Messenger.print("Ответ получен", TextColor.BLUE)

                if (response.key == key && response::class.java == Response::class.java) {
                    response as Response
                    if (response.success) {
                        try {
                            updateCommandList(Json.decodeFromString(response.message))
                        } catch (_: SerializationException) {
                            Messenger.print("Ответ сервера некорректен", TextColor.RED); Thread.sleep(1000); continue
                        } catch (_: IllegalArgumentException) {
                            Messenger.print("Ответ сервера некорректен", TextColor.RED); Thread.sleep(1000); continue
                        }
                    } else { Messenger.print(response.message) }

                    if (response.necessaryTask != null)
                        get<Task<ChannelClientWorker>>(named(response.necessaryTask!!)).execute(this)

                    return@Task
                }

                Messenger.print("Ответ сервера некорректен", TextColor.RED); Thread.sleep(1000)
            }

            Messenger.print("Не удалось получить корректный ответ от сервера", TextColor.RED)
        }
    }

    factory(named("logIn")) {
        Task<ChannelClientWorker> {
            val r = Reader()
            Messenger.print("Введите данные для входа")
            Messenger.inputPrompt("Логин")
            val login = r.readNotEmptyString() ?: exitProcess(0)
            Messenger.inputPrompt("Пароль")
            val passwd = r.readNotEmptyString() ?: exitProcess(0)

            val key = generateKey()
            val response = sendAndReceive(Request("log_in", key, CommandArgument(Json.encodeToString(Pair(login, passwd)))))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)
            if (response.key == key && response::class.java == Response::class.java) {
                response as Response
                if (response.success) {
                    token = response.message
                    Messenger.print("Вход выполнен успешно. Токен: ${response.message}")
                } else {
                    Messenger.print("Ошибка авторизации: ${response.message}", TextColor.RED)
                }

                if (response.necessaryTask != null) {
                    get<Task<ChannelClientWorker>>(named(response.necessaryTask!!)).execute(this)
                }
            } else {
                Messenger.print("Ответ сервера некорректен", TextColor.RED)
            }
        }
    }

    factory(named("register")) {
        Task<ChannelClientWorker> {
            val r = Reader()
            Messenger.print("Введите данные для регистрации")
            Messenger.inputPrompt("Логин")
            val login = r.readNotEmptyString() ?: exitProcess(0)
            Messenger.inputPrompt("Пароль")
            val passwd = r.readNotEmptyString() ?: exitProcess(0)

            val key = generateKey()
            val response = sendAndReceive(Request("register", key, CommandArgument(Json.encodeToString(Pair(login, passwd)))))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)
            if (response.key == key && response::class.java == Response::class.java) {
                response as Response
                if (response.success) {
                    token = response.message
                    Messenger.print("Регистрация прошла успешно. Токен: ${response.message}")
                } else {
                    Messenger.print("Ошибка регистрации: ${response.message}", TextColor.RED)
                }

                if (response.necessaryTask != null) {
                    get<Task<ChannelClientWorker>>(named(response.necessaryTask!!)).execute(this)
                }
            } else {
                Messenger.print("Ответ сервера некорректен", TextColor.RED)
            }
        }
    }

    factory(named("checkToken")) {
        Task<ChannelClientWorker> {
            Messenger.inputPrompt("Введите токен")
            val r = Reader()
            while (true) {
                token = r.readStringOrNull()
                if (token != null) break
                Messenger.inputPrompt("Повторите ввод", color = TextColor.RED)
            }
            for (i in 1..5) {
                val key = generateKey()
                val args = CommandArgument()
                args.setToken(token)
                val response = sendAndReceive(Request("check_token", key, args))
                if (response != null) {
                    processMessage(response, key)
                    return@Task
                }

                Messenger.print("Сервер не отвечает, попытка повторной отправки запроса", TextColor.RED)
            }

            Messenger.print("Сервер не отвечает")
        }
    }

    factory(named("identify")) {
        Task<ChannelClientWorker> {
            val r = Reader()
            Messenger.print("Требуется вход.", TextColor.RED)
            Messenger.inputPrompt("Введите вариант подключения к серверу (1 - авторизоваться, 2 - зарегистрироваться, 3 - использовать токен)")
            var v: String
            while (true) {
                v = r.readString()
                when (v) {
                    "1" -> {
                        get<Task<ChannelClientWorker>>(named("logIn")).execute(this); break
                    }

                    "2" -> {
                        get<Task<ChannelClientWorker>>(named("register")).execute(this); break
                    }

                    "3" -> {
                        get<Task<ChannelClientWorker>>(named("checkToken")).execute(this); break
                    }

                    else -> Messenger.print("Повторите ввод: ", TextColor.RED, false)
                }
            }
        }
    }
}

val executeCommandTasks = module {
    factory(named("help")) {
            (args: CommandArgument) ->
        Task<ChannelClientWorker> {
            ArgumentValidator(listOf()).check(args)

            Messenger.print(String.format("%-40s", "help:"), newLine = false)
            Messenger.print("вывести список доступных команд", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "exit:"), newLine = false)
            Messenger.print("завершить работу клиента", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "execute_script:"), newLine = false)
            Messenger.print("исполнить скрипт", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "check_connect:"), newLine = false)
            Messenger.print("проверить соединение", TextColor.BLUE)

            Messenger.print(String.format("%-40s", "update_command_list:"), newLine = false)
            Messenger.print("обновить список команд", TextColor.BLUE)

            for (commandInfo in getCommandList()) {
                Messenger.print(String.format("%-40s", "${commandInfo.name}:"), newLine = false)
                Messenger.print(commandInfo.info, TextColor.BLUE)
            }
        }
    }

    factory(named("exit")) {
            (args: CommandArgument) ->
        Task<ChannelClientWorker> {
            ArgumentValidator(listOf()).check(args)
        }
    }

    factory(named("check_connect")) {
        (args: CommandArgument) ->
        Task<ChannelClientWorker> {
            ArgumentValidator(listOf()).check(args)
            val key = generateKey()
            args.setToken(token)
            val response = sendAndReceive(Request(key, "check_token", args))
            if (response != null) {
                processMessage(response, key)
                return@Task
            }
        }
    }

    factory(named("execute_script")) {
            (args: CommandArgument) ->
        Task<ChannelClientWorker> {
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
                        get<Task<ChannelClientWorker>>(named(command.first)) { parametersOf(command.second) }.execute(
                            this
                        )

                    else -> {
                        val key = generateKey()
                        command.second.setToken(token)
                        val response =
                            sendAndReceive(Request(command.first, key, command.second))
                    }
                }
            }
        }
    }
}

