using AudioWebApi.Controllers;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;
using System.IO;

namespace AudioWebApi.Test
{
    [TestClass]
    public class AudioControllerTests
    {
        [TestMethod]
        public void PostCheckFile200()
        {
            // Arrange
            var fileName = "Musics/Found.mp3";
            FileStream fs = new FileStream(fileName, FileMode.Open);
            var fileMock = new Mock<IFormFile>();
            fileMock.Setup(_ => _.OpenReadStream()).Returns(fs);
            fileMock.Setup(_ => _.FileName).Returns(fileName);
            fileMock.Setup(_ => _.Length).Returns(fs.Length);

            var moqService = new Mock<IACRCloud>();
            moqService.Setup(x => x.GetInfo(fs)).Returns(new InfoResponse
            {
                Status = ResponseStatus.OK,
                Artist = "Starkov,ОриГами",
                Title = "Без лишних слов"
            });

            AudioController controller = new AudioController(moqService.Object);

            // Act
            var response = controller.Post(fileMock.Object) as ObjectResult;
            var musicResponse = response.Value as MusicResponse;

            // Assert
            Assert.AreEqual(200, response.StatusCode);
            Assert.AreEqual("Starkov,ОриГами", musicResponse.Artist);
            Assert.AreEqual("Без лишних слов", musicResponse.Title);
        }

        [TestMethod]
        public void PostCheckFileNotMp3()
        {
            var fileName = "Musics/TempFile.txt";
            FileStream fs = new FileStream(fileName, FileMode.Open);
            var fileMock = new Mock<IFormFile>();
            fileMock.Setup(_ => _.OpenReadStream()).Returns(fs);
            fileMock.Setup(_ => _.FileName).Returns(fileName);
            fileMock.Setup(_ => _.Length).Returns(fs.Length);

            var moqService = new Mock<IACRCloud>();

            AudioController controller = new AudioController(moqService.Object);

            // Act
            var response = controller.Post(fileMock.Object) as ObjectResult;
            var musicResponse = response.Value as MusicResponse;

            // Assert
            Assert.AreEqual(400, response.StatusCode);
            Assert.AreEqual(null, musicResponse.Artist);
            Assert.AreEqual(null, musicResponse.Title);
        }

        [TestMethod]
        public void PostCheckFileNull()
        {
            // Arrange
            var moqService = new Mock<IACRCloud>();
            AudioController controller = new AudioController(moqService.Object);

            // Act
            var response = controller.Post(null) as ObjectResult;

            // Assert
            Assert.AreEqual(400, response.StatusCode);
        }

        [TestMethod]
        public void PostCheckFile404()
        {
            // Arrange
            var fileName = "Musics/NotFoud.mp3";
            FileStream fs = new FileStream(fileName, FileMode.Open);
            var fileMock = new Mock<IFormFile>();
            fileMock.Setup(_ => _.OpenReadStream()).Returns(fs);
            fileMock.Setup(_ => _.FileName).Returns(fileName);
            fileMock.Setup(_ => _.Length).Returns(fs.Length);

            var moqService = new Mock<IACRCloud>();
            moqService.Setup(x => x.GetInfo(fs)).Returns(new InfoResponse
            {
                Status = ResponseStatus.NotFound,
                Artist = null,
                Title = null
            });

            AudioController controller = new AudioController(moqService.Object);

            // Act
            var reponse = controller.Post(fileMock.Object) as ObjectResult;
            var musicResponse = reponse.Value as MusicResponse;

            // Assert
            Assert.AreEqual(404, reponse.StatusCode);
            Assert.AreEqual(null, musicResponse.Artist);
            Assert.AreEqual(null, musicResponse.Title);
        }
    }
}