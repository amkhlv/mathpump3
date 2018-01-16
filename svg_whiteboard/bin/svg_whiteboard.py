
import time
import shutil
from watchdog.observers import Observer
from watchdog.events import *
from datetime import datetime, timedelta
import json
import subprocess
from multiprocessing import Process
import argparse

delay = 0.05

def update(ioqml,s,d):
        time.sleep(delay)
        shutil.copyfile(s,d)
        ioqml.stdin.write((json.dumps({"svgfile": os.path.abspath(d)}) + "\n").encode('utf-8'))
        ioqml.stdin.flush()

class OurHandler (FileSystemEventHandler):
    def __init__(self, wdir, tdir, ioqml):
        self.wdir = wdir
        self.tdir = tdir
        self.ioqml = ioqml
        self.prev_events = {}
    def on_created(self, event):
        print("created !")
    def on_modified(self,ev: FileModifiedEvent):
        s = ev.src_path
        dtn = datetime.now()
        td = timedelta.max if not(s in self.prev_events.keys()) else dtn - self.prev_events[s]
        if td.microseconds > 1000000 * delay  and ev.src_path[-4:] == ".svg":
            #print(s)
            self.prev_events[s] = dtn
            for f in os.listdir(self.tdir):
                if os.path.isfile(self.tdir + "/" + f): os.remove(self.tdir + "/" + f)
            tmp_filepath = self.tdir + "/" + dtn.isoformat() + ".svg"
            p = Process(target = update, args = [self.ioqml,s,tmp_filepath])
            p.start()
            p.join()
        #else: print("---" + s + "---" + str(td.microseconds))

if __name__ == "__main__":
    aparser = argparse.ArgumentParser()
    aparser.add_argument("-t", "--tmpdir", type=str, action="store", help="tmp directory, default=WATCHDIR/tmp")
    aparser.add_argument("-q", "--qml", type=str, action="store", required = True, help="path to the ioqml's QML file")
    aparser.add_argument("indir", type=str, metavar="WATCHDIR", action="store", help="directory to watch")
    args = aparser.parse_args()
    path = args.indir
    qml = args.qml
    tmp_path = args.indir + "/tmp/" if not args.tmpdir else args.tmpdir
    if not os.path.exists(tmp_path): os.makedirs(tmp_path)
    ioqml = subprocess.Popen(
        ["ioqml", args.qml],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE
    )
    event_handler = OurHandler(path, tmp_path, ioqml)
    observer = Observer()
    wtch = observer.schedule(event_handler, path, recursive=False)
    observer.start()
    while True:
        time.sleep(delay)
        if ioqml.poll() is not None: break
    observer.stop()
