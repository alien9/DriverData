
rm ../app/src/main/assets/driver-angular/*.*
cp -r dist/driver-angular ../app/src/main/assets/

sed -i -e "s/href=\"https:\/\/cdn.jsdelivr.net\/npm\/font-awesome@4.7.0\/css/href=\"extras/g" \
 -e "s/https:\/\/maxcdn.bootstrapcdn.com\/bootstrap\/3.4.1\/css/extras/g" \
 -e "s/https:\/\/ajax.googleapis.com\/ajax\/libs\/jquery\/3.5.1/extras/g" \
 -e "s/https:\/\/maxcdn.bootstrapcdn.com\/bootstrap\/3.4.1\/js/extras/g" \
 -e "s/<base href=\".\">/<base href=\"#\">/g" \
../app/src/main/assets/driver-angular/index.html

