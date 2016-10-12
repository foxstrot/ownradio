// Компания "Нетвокс Лаб"
// ExecuteProcedurePostgreSQLController.cs
// Контроллер класса ExecuteProcedurePostgreSQL.
// Получает параметры в GET запросе и передает их в функцию для запуска хранимой процедуры базы данных
// Александра Полунина
// Создан:  2016-10-11 10:43
// Изменен: 2016-10-11 16:00
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using OwnRadio.Web.Api.Infrastructure;
using OwnRadio.Web.Api.Models;
using System;

namespace OwnRadio.Web.Api.Controllers
{
    [Route("api/[controller]/[action]")]
    public class ExecuteProcedurePostgreSQLController : Controller
    {
        public Settings settings { get; }

        public ExecuteProcedurePostgreSQLController(IOptions<Settings> settings)
        {
            this.settings = settings.Value;
        }

        //Выполняет слияние статистики прослушивания треков на разных устройствах по двум User ID одного пользователя
        // GET api/ExecuteProcedurePostgreSQL/MergeUserID/12345678-1234-1234-1234-123456789012,12345678-1234-1234-1234-123456789012
        [HttpGet("{userIDOld},{userIDNew}")]
        public int MergeUserID(Guid userIDOld, Guid userIDNew)
        {
            var executeProcedure = new ExecuteProcedurePostgreSQL(settings.connectionString);
            var rowsCount = executeProcedure.MergeUserID(userIDOld, userIDNew);
            return rowsCount;
        }

        //Сохраняет нового пользователя и устройство
        // GET api/ExecuteProcedurePostgreSQL/RegisterDevice/12345678-1234-1234-1234-123456789012,UserName,DeviceName
        [HttpGet("{deviceID},{userName},{deviceName}")]
        public int RegisterDevice(Guid deviceID, String userName, String deviceName)
        {
            var executeProcedure = new ExecuteProcedurePostgreSQL(settings.connectionString);
            var rowsCount = executeProcedure.RegisterDevice(deviceID, userName, deviceName);
            return rowsCount;
        }

        //Получает ID пользователя по DeviceID
        // GET api/ExecuteProcedurePostgreSQL/GetUserId/2601ef2c-c430-4154-9e58-f1be0affe8a8
        [HttpGet("{deviceID}")]
        public Guid GetUserId(Guid deviceID)
        {
            var executeProcedure = new ExecuteProcedurePostgreSQL(settings.connectionString);
            return executeProcedure.GetUserId(deviceID);
        }

        //Переименовывает пользователя
        // GET api/ExecuteProcedurePostgreSQL/RenameUser/12345678-1234-1234-1234-123456789012,NewUserName
        [HttpGet("{userID},{newUserName}")]
        public int RenameUser(Guid userID, String newUserName)
        {
            var executeProcedure = new ExecuteProcedurePostgreSQL(settings.connectionString);
            var rowsCount = executeProcedure.RenameUser(userID, newUserName);
            return rowsCount;
        }
    }
}
