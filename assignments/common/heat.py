import numpy as np
from math import sqrt

# heat sources used to create the border values
heat_sources = [ { 'x': 0.0, 'y': 0.0, 'range': 1.0, 'temp': 2.5 }, {'x': 0.5, 'y': 1.0, 'range': 1.0, 'temp': 2.5} ] 


# Apply the heat sources defined above. The handling of each border could be improved, but it works...
# Note that the order of the sources is important, values are overwritten, not modified
# There is no need to modify this function unless you find a bug
def apply_hs(m, hs, border):
    if border == 'top' or border == 'bottom':
        r = (0, m.shape[0])
    elif border == 'left' or border == 'right':
        r = (1, m.shape[0]-1)
    else:
        return

    for i in range(*r):
        if border == 'top':
            dist = sqrt(pow(i / (m.shape[0] - 1) - hs['x'], 2) + pow(hs['y'], 2))
        elif border == 'bottom':
            dist = sqrt(pow(i / (m.shape[0] - 1) - hs['x'], 2) + pow(1 - hs['y'], 2))
        elif border == 'left':
            dist = sqrt(pow(hs['x'], 2) + pow(i / (m.shape[0] - 1) - hs['y'], 2))
        elif border == 'right':
            dist = sqrt(pow(1 - hs['x'], 2) + pow(i / (m.shape[0] - 1) - hs['y'], 2))

        if dist <= hs['range']:
            if border == 'top':
                m[0, i] += (hs['range'] - dist) / hs['range'] * hs['temp']
            elif border == 'bottom':
                m[-1, i] += (hs['range'] - dist) / hs['range'] * hs['temp']
            elif border == 'left':
                m[i, 0] += (hs['range'] - dist) / hs['range'] * hs['temp']
            elif border == 'right':
                m[i,-1] += (hs['range'] - dist) / hs['range'] * hs['temp']


# Create a grid and apply the heat sources
def init(hs, n):
    num_p = n + 2

    m = np.zeros((num_p, num_p))
    for hs in heat_sources:
        apply_hs(m, hs, 'top')
        apply_hs(m, hs, 'bottom')
        apply_hs(m, hs, 'left')
        apply_hs(m, hs, 'right')

    return m


# Run a single iteration step, return the residual/error
def gauss_seidel_step(m):
    s = 0.0
    for i in range(1, m.shape[0]-1):
        for j in range(1, m.shape[1]-1):
            tmp = m[i, j]
            m[i, j] = (m[i, j-1] + m[i, j+1] + m[i-1, j] + m[i+1, j]) / 4
            diff = tmp - m[i, j]
            s += diff**2 # Adjusted for correctness: previously s += diff**2 * diff**2

    return s


# Run at most maxiter iterations. End if the residual/error is lower than the tolerance
def gauss_seidel(m, maxiter, tol):
    for i in range(maxiter):
        res = gauss_seidel_step(m)
        if res < tol:
            break
    return (res, i)


if __name__ == '__main__':
    m = init(heat_sources, 100)
    res, i = gauss_seidel(m, 25000, 0.00005)
    print(f'residual = {res} after {i} iterations')