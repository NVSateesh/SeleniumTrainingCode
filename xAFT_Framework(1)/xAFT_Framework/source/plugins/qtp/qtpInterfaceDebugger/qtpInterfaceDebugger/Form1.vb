Public Class Form1
    Public firsttime = True
    Public QTP
    Private Sub setFirstTime()
        If firsttime Then firsttime = False
    End Sub

    Private Sub Button1_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Button1.Click
        Dim allItems

        If firsttime Then
            QTP = CreateObject("com.ags.aft.Engine.QTP.QTPInterface")
            QTP.Initialize("key", TextBox1.Text, TextBox3.Text, "", TextBox1.Text & "\objectRepository")
            QTP.syncListner()
            setFirstTime()
        End If
        Dim sLOG
        For Each line In RichTextBox1.Lines
            allItems = Split(line.ToString(), ",")
            Try
                sLOG = QTP.ExecuteAction(allItems(0), allItems(1), allItems(2))
            Catch ex As Exception
                sLOG = "EXCEPTION: " & ex.Message
            End Try
            If Trim(sLOG) <> "" Then RichTextBox2.Text = RichTextBox2.Text & vbLf & "Statement: " & line & "; " & sLOG
        Next
        '
        'QTP.ExecuteAction("open", "novalue", "www.amazon.com")
        'QTP.ExecuteAction("type", "txtSearch", "47LM7600")
        'QTP.ExecutteAction("click", "btnGo", "novalue")
        'QTP.ExecuteAction("mclick", "lnkLGCinemaScreen47LM7600", "novalue")
        'QTP.ExecuteAction("click", "imgAddToShoppingCart", "novalue")

    End Sub

  
    Private Sub RichTextBox1_TextChanged(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles RichTextBox1.TextChanged

    End Sub

    Private Sub Button2_Click(ByVal sender As System.Object, ByVal e As System.EventArgs)

    End Sub

    Private Sub Button2_Click_1(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles Button2.Click
        QTP.StopQTP()
        firsttime = True
    End Sub
End Class
