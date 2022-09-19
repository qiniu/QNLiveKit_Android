echo "编译qliveUIkit sdk"
echo "----------------------"
sed -i "" "s#^buidUIKitSH=.*#buidUIKitSH=1#g"  gradle.properties
./../../gradlew :doc:sdkbuilder:makeAAR
sed -i "" "s#^buidUIKitSH=.*#buidUIKitSH=0#g"  gradle.properties
