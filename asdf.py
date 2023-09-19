letters = [ord('A') + i for i in range(10)]
num = [str(i) for i in range(10)]

listC = []

start = 0
step = 1

for i in range(len(letters)):
    end = start + step
    listC.extend(num[start:end])
    listC.extend(letters[start:end])

    start = end
    step += 1

print(listC)