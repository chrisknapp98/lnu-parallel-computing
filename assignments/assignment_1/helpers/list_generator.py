import json
import random

def generate_list_of_random_integers(n, a, b):
    return [random.randint(a, b) for _ in range(n)]

def save_list_to_json_file(filename, lst):
    with open(filename, 'w') as f:
        json.dump(lst, f)

def save_list_to_new_json_file(filename, lst):
    with open(filename, 'w') as f:
        json.dump(lst, f)

def main():
    n, a, b = 10**3, 0, 10**8
    list = generate_list_of_random_integers(n, a, b)
    save_list_to_json_file('assignments/assignment_1/inputs/random_integers_3_5.json', list)

if __name__ == '__main__':
    main()