import sys,os;
if len(sys.argv) == 1:
  print 'need one command: start/stop/restart.';
  sys.exit();
cmd = sys.argv[1];
if cmd == 'start':
  os.system('java -jar /opt/apps/resin-pro-4.0.36/lib/resin.jar start --conf /opt/conf/resin/resin.xml');
elif cmd == 'stop':
  os.system('java -jar /opt/apps/resin-pro-4.0.36/lib/resin.jar stop --conf /opt/conf/resin/resin.xml');
elif cmd == 'restart':
  os.system('java -jar /opt/apps/resin-pro-4.0.36/lib/resin.jar restart --conf /opt/conf/resin/resin.xml');
else:
  print 'nothing happen';