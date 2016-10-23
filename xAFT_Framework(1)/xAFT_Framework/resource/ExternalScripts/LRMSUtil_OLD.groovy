// Find whether a string contains a particular substring
def strFind(def selenium, def log4j,def source,def searchItem)
{
log4j.info(source)

if(source.toLowerCase().contains(searchItem.toLowerCase()))
{
	return true	
}
else	
{
	return false
}
}



