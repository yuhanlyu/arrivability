import matplotlib
import string
import matplotlib.pyplot as plt
from matplotlib.path import Path
import matplotlib.patches as patches
from matplotlib.backends.backend_pdf import PdfPages

#im = plt.imread('Hanover.png')
#implot = plt.imshow(im, extent=[0,500,10,510])
linetype = [[],[5,4],[5,4,2,4],[5,4,2,4,2,4],[2,4],[2,2]]
linecolor = ['b','g','r','c','m','y','k','0.5']
usecolor = 1

def main():
    plt.axes()

    myfile = open('demos/demo_4-Arrivability_FixedRadius')
    
    m,n = [int(x) for x in myfile.readline().split()]
    num = int(myfile.readline().split()[0])
   
    points = [x for x in myfile.readline().split(') ')]
    x = []
    y = []
    for i in range(0,num):
        new_cor=points[i].split(' ')
        new_cor[0]=new_cor[0][1:]
        x.append(float(new_cor[0]))
        y.append(float(new_cor[1]))
        circle = plt.Circle(((y[i])*5,(n-x[i])*5),0.1)
        plt.gca().add_patch(circle)
    
    startpoint = myfile.readline().split(' ')
    sx = float(startpoint[0][1:])
    sy = float(startpoint[1][:-2])

    endpoint = myfile.readline().split(' ')
    tx = float(endpoint[0][1:])
    ty = float(endpoint[1][:-2])

    k = int(myfile.readline().split()[0])
    for i in range(k):
        path = myfile.readline().split(') ')
        pathx = []
        pathy = []
        for j in range(len(path)-1):
            cor=path[j].split(' ')
            cor[0]=cor[0][1:]
            pathx.append(float(cor[0]))
            pathy.append(float(cor[1]))
        for j in range(len(path)-2):
            if (usecolor==1):
                line = plt.Line2D(((pathy[j])*5,(pathy[j+1])*5), ((n-pathx[j])*5, (n-pathx[j+1])*5), color=linecolor[i],lw=2)
            else:
                line = plt.Line2D(((pathy[j])*5,(pathy[j+1])*5), ((n-pathx[j])*5, (n-pathx[j+1])*5), dashes=linetype[i],lw=2)
            plt.gca().add_line(line)

    circle = plt.Circle(((sy)*5,(n-sx)*5),3,fc='r')
    plt.gca().add_patch(circle)
    circle = plt.Circle(((ty)*5,(n-tx)*5),3,fc='b')
    plt.gca().add_patch(circle)

# demonstrate failure group with radius = 2

#    centerx = 2
#    centery = 42
#    circle = plt.Circle((centery*5,(n-centerx)*5),2,fc='r')
#    plt.gca().add_patch(circle)
#    line = plt.Line2D(((centery-2.4)*5,centery*5), ((n-centerx)*5, (n-centerx+2.4)*5), color='r',lw=2)
#    plt.gca().add_line(line)
#    line = plt.Line2D(((centery-2.4)*5,centery*5), ((n-centerx)*5, (n-centerx-2.4)*5), color='r',lw=2)
#    plt.gca().add_line(line)
#    line = plt.Line2D(((centery+2.4)*5,centery*5), ((n-centerx)*5, (n-centerx+2.4)*5), color='r',lw=2)
#    plt.gca().add_line(line)
#    line = plt.Line2D(((centery+2.4)*5,centery*5), ((n-centerx)*5, (n-centerx-2.4)*5), color='r',lw=2)
#    plt.gca().add_line(line)

    plt.axis('off')
    plt.axis('scaled')
    
# draw polygonal obstacles
    myobstaclef = open('files/random_map')
    m,n = [int(x) for x in myobstaclef.readline().split()]
    num = int(myobstaclef.readline().split()[0])

    codes = [Path.MOVETO,
             Path.LINETO,
             Path.LINETO,
             Path.LINETO,
             Path.CLOSEPOLY,
             ]
             
    for i in range(num):
        sides = int(myobstaclef.readline().split()[0])
        verts = []
        for j in range(sides):
            nx, ny = [int(x) for x in myobstaclef.readline().split()]
            verts.append((ny*5,(n-nx)*5))
        verts.append(verts[0])
        path = Path(verts,codes)
#        fig = plt.figure()
#ax = fig.add_subplot(111)
        patch = patches.PathPatch(path, facecolor='#D24905', lw=2)
        plt.gca().add_patch(patch)

    plt.savefig('4-Arrivability_FixedRadius.pdf', transparent = True)
    plt.show()

if __name__ == '__main__':
    main() 
