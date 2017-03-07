#include "mainwindow.h"
#include "ui_mainwindow.h"

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    tcpServer = new QTcpServer(this);

    connect(tcpServer, SIGNAL(newConnection()), this, SLOT(newConnection()));
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_pbStartListening_clicked(bool checked)
{
    log(checked ? "started" : "closed");

    int port = ui->leServerPort->text().toInt();

    if(checked) {
        if (!tcpServer->listen(QHostAddress::Any, port)) {
            qDebug() <<  QObject::tr("Unable to start the server: %1.")
                         .arg(tcpServer->errorString());
        } else {
            qDebug() << "Server successfully started!";
        }
    } else {
        tcpServer->close();
        qDebug() << "Server closed!";
    }
}

void MainWindow::newConnection()
{
    log("New connection!");

    clientConnection = tcpServer->nextPendingConnection();
    connect(clientConnection, SIGNAL(readyRead()), this, SLOT(readyRead()));
}

void MainWindow::readyRead()
{
    log("Receive:");
    log(clientConnection->readAll());
}

void MainWindow::on_pbConnect_clicked(bool checked)
{
    log(checked ? "connected" : "disconnected");

    if(checked) {
        qDebug() << "connected!";
        int port = ui->lePort->text().toInt();
        QString host = ui->leHost->text();

        connection = new QTcpSocket(this);
        connection->connectToHost(host, port);

        if(connection->isOpen()) {
            ui->pbSend->setEnabled(true);
            ui->ptMessage->setEnabled(true);
        } else {
            ui->pbConnect->setChecked(false);
        }

    } else {
        qDebug() << "disconnected!";
        connection->close();

        ui->pbSend->setEnabled(false);
        ui->ptMessage->setEnabled(false);
    }
}

void MainWindow::on_pbSend_clicked()
{
    QByteArray data;
    data.append(ui->ptMessage->toPlainText());

    log("Send: ");
    log(data.data());

    connection->write(data, data.length());
}

void MainWindow::log(const QString &message)
{
    ui->textBrowser->append(message);
}
