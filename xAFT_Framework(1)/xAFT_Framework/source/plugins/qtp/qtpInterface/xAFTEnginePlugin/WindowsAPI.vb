'*********************************************************************************************************************************
'Class Name         :    WindowsAPI
'Purpose            :	 To access windows api from vbscript/any com accessible lang. It provides wrapper to some common win api as
'                        we have limitations with data types acces from vb script. So these wrappers use high level lang to 
'                        wrap and support data types accessible from vb script
'Parameteres		:	
'Author				: 	  Pradeep Buddaraju
'Date				:	  Dec-2013
'*********************************************************************************************************************************
Public Class WindowsAPI
    Public Sub New()
        '//Dummy
    End Sub

    Structure POINTAPI ' This holds the logical cursor information
        Public x As Integer
        Public y As Integer

        Sub New(ByRef xPOS As Integer, ByRef yPOS As Integer)
            Me.x = xPOS
            Me.y = yPOS
        End Sub

    End Structure

    Declare Sub GetCursorPos Lib "User32.dll" (ByRef lptr As POINTAPI)

    Function getCursor()
        Dim pos(1)
        Dim rect As POINTAPI
        GetCursorPos(rect)
        pos(0) = rect.x
        pos(1) = rect.y
        getCursor = pos
    End Function
End Class