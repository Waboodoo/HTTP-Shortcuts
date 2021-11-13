import re
from os import listdir
from os import makedirs
from os.path import abspath
from os.path import basename
from os.path import dirname
from os.path import isfile
from os.path import join

CURRENT_DIR = dirname(abspath(__file__))

INPUT_DIR = join(join(CURRENT_DIR, '..'), 'docs')
OUTPUT_DIR = join(join(CURRENT_DIR, '..'), 'processed-docs')


def main():
    makedirs(OUTPUT_DIR, exist_ok=True)
    markdown_files = _get_markdown_files()

    for in_file in markdown_files:
        out_file = _get_processed_file_name(in_file)

        with open(in_file, 'r') as ifp:
            with open(out_file, 'w') as ofp:
                while True:
                    line = ifp.readline()
                    if not line:
                        break
                    ofp.write(_process_line(line))


def _get_markdown_files():
    files = [
        join(INPUT_DIR, file)
        for file in listdir(INPUT_DIR)
    ]
    return [
        file
        for file in files
        if isfile(file) and file.endswith('.md')
    ]


def _get_processed_file_name(in_file):
    return join(OUTPUT_DIR, basename(in_file))


def _process_line(line):
    # Process markdown links to other markdown files
    result = re.sub(r'(\[[^]]+]\([^)]+)\.md([^)]*\))', r'\1\2', line)
    # Process markdown image references
    result = re.sub(r'(!\[[^]]+]\()\.\.(/assets/[^)]+\))', r'\1\2', result)
    # Process HTML image references
    result = re.sub(r'( src=")\.\.(/assets/[^"]+")', r'\1\2', result)

    return result


if __name__ == '__main__':
    main()
