#!/bin/sh
resin="/opt/resin-pro-4.0.51"
if [ ! -d "$resin" ]; then
echo "please install resin4 first. thanks"
exit 0
fi

if type -p java; then
    java_home=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    java_home="$JAVA_HOME/bin/java"
else
    echo "please install jdk first. thanks"
    exit 0
fi

version=$("$java_home" -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo jdk version "$version"

#cd /usr/share/jenkins/workspace/flow/lx.springmvc/target

servers=(${resin.server})

#
serverCount=${#servers[@]}
serial_no=`date +%s`
i=0
while [ $i -lt $serverCount ]
do
    server=${servers[$i]}
    echo "deploy ${name} on $server"
    host=`echo $server | cut -d: -f1`
    port=`echo $server | cut -d: -f2`

    log="${log.path}/${name}-$port"
    conf="${conf.path}/${name}-$port.xml"
    data="${data.path}/${name}/$port"
    deploy="${deploy.path}/${name}/$port"
    webapp="$deploy/webapp"
    shell="${bin.path}/${name}-$port.sh";

    ssh resin@$host "mkdir -p ${bin.path}"
    ssh resin@$host "mkdir -p ${conf.path}"
    ssh resin@$host "mkdir -p $data"
    ssh resin@$host "mkdir -p $log"
    ssh resin@$host "mkdir -p $webapp"

    #create deploy dir
    ssh resin@$host "chown -R resin $data"
    ssh resin@$host "chown -R resin $log"
    
    #stop server
    while [ `ssh resin@$host "ps -ef | grep $conf | grep -v grep | wc -l"` -gt 0 ]
    do
        ssh resin@$host "$shell stop" || echo "$shell is not running"
        echo "waiting for $shell resin stop"
        sleep 1
    done
    
    #process shell script
    scp ${name}/deploy/start.sh resin@$host:${bin.path}
    ssh resin@$host "chmod +x ${bin.path}/start.sh"
    ssh resin@$host "mv -f ${bin.path}/start.sh $shell"
    ssh resin@$host "sed -i 's/##conf##/${conf//\//\\/}/' $shell"
    ssh resin@$host "chown resin $shell"

    #process config file, maybe cause resin restart
    ssh resin@$host "chown -R resin ${conf.path}"
    scp ${name}/deploy/resin4.xml resin@$host:${conf.path}
    ssh resin@$host "mv -f ${conf.path}/resin4.xml $conf"
    ssh resin@$host "sed -i 's/##port##/$port/g' $conf"

    #unpackage war
    ssh resin@$host "chown -R resin $deploy"    
    scp ${name}.war resin@$host:$deploy
    ssh resin@$host "cd $webapp && rm -rf * && /opt/apps/jdk/bin/jar xf $deploy/${name}.war"
    ssh resin@$host "cp $deploy/${name}.war $deploy/${name}-${serial_no}.war"
    
    #create soft link for data resources
    #ssh resin@$host "rm -f ${data.path}/${name}/js && ln -s $webapp/js ${data.path}/${name}/js"
    #ssh resin@$host "rm -f ${data.path}/${name}/css && ln -s $webapp/css ${data.path}/${name}/css"
    #ssh resin@$host "rm -f ${data.path}/${name}/img && ln -s $webapp/img ${data.path}/${name}/img"
    #ssh resin@$host "rm -f ${data.path}/${name}/sample && ln -s $webapp/sample ${data.path}/${name}/sample"

    ssh resin@$host "$shell start"
    
    i=$((i+1))
    if [ $i -ne ${serverCount} ]; then
        sleep 20
    fi

done
echo "all done."