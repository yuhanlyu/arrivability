
if __name__ == "__main__":
    print 45, 45
    print 25
    for i in [5 + 8 * k for k in xrange(5)]:
        for j in [5 + 8 * k for k in xrange(5)]:
            print 4
            print i, j
            print i + 4, j
            print i + 4, j + 4
            print i, j + 4
