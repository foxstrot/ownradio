#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QTcpServer>
#include <QTcpSocket>
#include <QMessageBox>
#include <QDebug>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void on_pbConnect_clicked(bool checked);
    void on_pbStartListening_clicked(bool checked);
    void on_pbSend_clicked();
    void newConnection();
    void readyRead();

private:
    void log(const QString&);

private:
    Ui::MainWindow *ui;

    QTcpServer *tcpServer;
    QTcpSocket *clientConnection;
    QTcpSocket *connection;
};

#endif // MAINWINDOW_H
