lsof -i tcp:$1 | awk 'NR!=1 {print $2}' | xargs kill 
