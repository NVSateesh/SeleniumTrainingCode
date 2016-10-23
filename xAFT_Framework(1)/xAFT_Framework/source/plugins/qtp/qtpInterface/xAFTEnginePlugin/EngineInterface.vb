Public Class EngineInterface

    Public Shared oConfigDict As Dictionary(Of String, String)

    Public Sub New()
        oConfigDict = New Dictionary(Of String, String)
    End Sub


    Public Function Initialize(ByVal privateKey, ByVal sRootPath, ByVal appName, ByVal BrowserType, ByVal ORPath)
    End Function

End Class
