import random

def random_generate(row, column, number):
    print row, column
    print number
    for i in xrange(number):
        print 4
        x, y = random.randrange(3, row - 3, 5), random.randrange(3, column-3, 5)
        (w, h) = 1, 5
        if random.randint(1, 2) == 1:
            (w, h) = (h, w)
        print x, y
        print x + w, y
        print x + w, y + h
        print x, y + h

if __name__ == "__main__":
    #row = input("Number of rows\n")
    #column = input("Number of columns\n")
    #number = input("Number of obstacles\n")
    row, column, number = 50, 50, 80
    random_generate(row, column, number)
