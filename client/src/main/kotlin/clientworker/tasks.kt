package clientworker

import command.ArgumentValidator
import command.CommandArgument
import iostreamers.Messenger
import iostreamers.Reader
import iostreamers.TextColor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import request.Request
import java.lang.Exception

val startTasks = module {
    factory(named("getCommandInfo")) {
        Task {
            Messenger.print("Запрос на получение актуального списка команд...")
            val response = sendAndReceive(Request("help", "HELP", user = user, passwd = token ?: password))
                ?: return@Task Messenger.print("Сервер не отвечает", TextColor.RED)

            Messenger.print("Ответ получен", TextColor.BLUE)

            if (response.requestKey == "") {
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
            ArgumentValidator(listOf()).check(args)
        }
    }
}
