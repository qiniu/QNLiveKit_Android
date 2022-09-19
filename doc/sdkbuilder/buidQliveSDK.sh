
echo "编译qlive 无UIsdk"
echo "----------------------"
sed -i "" "s#^buidQLiveSH=.*#buidQLiveSH=1#g"  gradle.properties
./../../gradlew :doc:sdkbuilder:makeAAR
sed -i "" "s#^buidQLiveSH=.*#buidQLiveSH=0#g"  gradle.properties
echo "----------------------"