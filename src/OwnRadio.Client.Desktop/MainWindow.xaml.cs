using System;
using System.Windows;
using OwnRadio.Client.Desktop.ViewModel;

namespace OwnRadio.Client.Desktop
{
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();

            OuterBorder.MouseLeftButtonDown += delegate { this.DragMove(); };
            BtnClose.Click += delegate { this.Close(); };
        }
    }
}
