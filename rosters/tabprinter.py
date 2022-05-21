with open("Period_1_APCS_Ferrante.txt","r") as f:
    data = f.read();


data = data.replace("          "," \t ")


with open("Period_1_APCS_Ferrante_new.txt","w") as f:
    f.write(data);
