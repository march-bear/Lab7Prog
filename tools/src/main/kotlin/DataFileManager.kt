import collection.CollectionWrapper
import exceptions.MethodCallException
import iostreamers.Messenger
import iostreamers.TextColor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.Logger
import organization.Organization
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class DataFileManager(
    private val collection: CollectionWrapper<Organization>,
    dataFile: File? = null,
    private val logger: Logger? = null,
) {
    private var dataHasBeenLoaded = false
    private val defaultFileIsUsed = dataFile == null
    private val dataFile: File = dataFile ?: File(DEFAULT_FILE_NAME)

    private fun readFile(): String {
        val stream = InputStreamReader(FileInputStream(dataFile))
        val data = stream.readText()
        stream.close()
        return data
    }

    private fun writeToFile(data: String) {
        val stream = OutputStreamWriter(FileOutputStream(dataFile))
        stream.write(data)
        stream.close()
    }

    fun loadData(): String {
        logger?.info("Загрузка коллекции")
        if (dataHasBeenLoaded) {
            logger?.error("Загрузка прервана - коллекция уже была загружена")
            throw MethodCallException("В загрузке отказано: коллекция уже была загружена")
        }

        var output = ""
        if (defaultFileIsUsed) {
            logger?.warn("Загрузчику не был передан путь к файлу. Загрузка будет произведена из файла $DEFAULT_FILE_NAME")
            output += Messenger.message(
                "ВНИМАНИЕ! На вход не подан не один файл! Загрузка будет произведена из файла по умолчанию\n",
                TextColor.YELLOW
            )
        }

        if (!checkFile(dataFile)) {
            logger?.warn("Файл $dataFile не найден")
            logger?.info("Файл $dataFile был создан")
            dataHasBeenLoaded = true
            return "$output$dataFile: файл был создан"
        }

        logger?.info("Чтение файла $dataFile")
        output += "Чтение файла с коллекцией..."
        val data = readFile()
        logger?.info("Чтение завершено")
        output += "\nЧтение завершено"

        collection.clear()

        if (data.isBlank()) {
            logger?.warn("Файл $dataFile пуст")
            logger?.info("Загрузка завершена")
            dataHasBeenLoaded = true
            return "${output}\nФайл пуст, загрузка завершена"
        }

        output += "\nЗагрузка данных в коллекцию...\n"

        val tmpCollection: CollectionWrapper<Organization>
        val moduleForPolymorphicSerializationAnyOrganization = SerializersModule {
            polymorphic(Any::class, Organization::class, Organization.serializer())
        }

        val jsonWithPolymorphicModule = Json { serializersModule = moduleForPolymorphicSerializationAnyOrganization }

        try {
            tmpCollection = jsonWithPolymorphicModule.decodeFromString(data)
        } catch (e: SerializationException) {
            logger?.error("Обнаружена синтаксическая ошибка, загрузка прервана")
            dataHasBeenLoaded = true
            return output + Messenger.message(
                "\n$dataFile: загрузка прервана вследствие обнаруженной синтаксической ошибки\n",
                TextColor.RED,
            )
        } catch (e: IllegalArgumentException) {
            logger?.error("Формат не соответствует типу коллекции, загрузка прервана")
            dataHasBeenLoaded = true
            return output + Messenger.message(
                "$\ndataFile: загрузка прервана вследствие несоответствия формата данных в файле типу коллекции\n",
                TextColor.RED,
            )
        }

        collection.initializationDate = tmpCollection.initializationDate

        for (elem in tmpCollection) {
            output += if (elem.objectIsValid()) {
                if (CollectionController.checkUniquenessFullName(elem.fullName, collection))
                    if (CollectionController.checkUniquenessId(elem.id, collection)) {
                        collection.add(elem)
                        logger?.info("В коллекцию добавлен элемент с id ${elem.id}")
                        "\nЭлемент ${Messenger.message(elem.id.toString(), TextColor.BLUE)}: " +
                                Messenger.message("добавлен", TextColor.BLUE)
                    } else {
                        logger?.error("Ошибка во время добавления нового элемента: id не уникален")
                        Messenger.message(
                            "\nОшибка во время добавления элемента в коллекцию: id не уникален",
                            TextColor.RED
                        )
                    }
                else {
                    logger?.error("Ошибка во время добавления нового элемента: полное имя не уникально")
                    Messenger.message(
                        "\nОшибка во время добавления элемента в коллекцию: полное имя не уникально",
                        TextColor.RED
                    )
                }
            } else {
                logger?.error("Ошибка во время добавления нового элемента: элемент не валиден")
                Messenger.message(
                    "\nОшибка во время добавления элемента в коллекцию: элемент невалиден",
                    TextColor.RED
                )
            }
        }

        dataHasBeenLoaded = true
        logger?.info("Загрузка коллекции из файла $dataFile завершена")
        return "$output\n\nЗагрузка завершена"
    }

    fun saveData(): Boolean {
        logger?.info("Сохранение коллекции в файл $dataFile")
        if (!dataHasBeenLoaded) {
            logger?.error("В сохранении отказано: коллекция ещё не была загружена")
            throw MethodCallException("В сохранении отказано: коллекция ещё не была загружена")
        }

        val moduleForPolymorphicSerializationAnyOrganization = SerializersModule {
            polymorphic(Any::class, Organization::class, Organization.serializer())
        }

        val jsonWithPolymorphicModule = Json { serializersModule = moduleForPolymorphicSerializationAnyOrganization }

        val data = jsonWithPolymorphicModule.encodeToString(collection)
        writeToFile(data)
        logger?.info("Коллекция сохранена в файл $dataFile")
        return true
    }

    private fun checkFile(file: File): Boolean {
        if (!this.dataFile.exists()) {
            this.dataFile.createNewFile()
            return false
        } else if (!this.dataFile.canWrite() || !this.dataFile.canRead()) {
            logger?.error("Загрузка невозможна: пользователь не обладает достаточными правами для доступа к файлу $file")
            throw FileNotFoundException(
                "Пользователь не обладает достаточными правами для доступа к файлу $file"
            )
        } else if (this.dataFile.isDirectory) {
            logger?.error("Загрузка невозможна: $file - директория")
            throw FileNotFoundException("$file - директория")
        }
        return true
    }

    companion object {
        const val DEFAULT_FILE_NAME = "data.json"
    }
}