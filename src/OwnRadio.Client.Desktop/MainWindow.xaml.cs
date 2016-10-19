using System;
using System.Windows;

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
