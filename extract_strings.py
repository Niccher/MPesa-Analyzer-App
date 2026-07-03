import os
import re

LAYOUT_DIR = "app/src/main/res/layout"
STRINGS_FILE = "app/src/main/res/values/strings.xml"

# Read existing strings
with open(STRINGS_FILE, "r") as f:
    strings_content = f.read()

new_strings = {}
counter = 1

for root, _, files in os.walk(LAYOUT_DIR):
    for file in files:
        if file.endswith(".xml"):
            file_path = os.path.join(root, file)
            with open(file_path, "r") as f:
                content = f.read()
            
            # Find all hardcoded android:text attributes
            # Match android:text="something" where something doesn't start with @
            matches = re.findall(r'android:text="([^@][^"]*?)"', content)
            
            if matches:
                for match in set(matches):
                    # Generate a key
                    key = "extracted_string_" + str(counter)
                    # To make it readable, maybe use first few chars
                    clean_name = re.sub(r'[^a-zA-Z0-9]', '_', match.strip())[:20].strip('_').lower()
                    if clean_name:
                        key = "str_" + clean_name + "_" + str(counter)
                    counter += 1
                    
                    new_strings[key] = match
                    
                    # Replace in content
                    content = content.replace(f'android:text="{match}"', f'android:text="@string/{key}"')
                
                with open(file_path, "w") as f:
                    f.write(content)

# Append new strings to strings.xml
if new_strings:
    # Find the closing </resources>
    if "</resources>" in strings_content:
        insert_idx = strings_content.rfind("</resources>")
        
        new_tags = "\n"
        for key, val in new_strings.items():
            # escape ampersands if needed, though they might already be escaped
            val = val.replace("&", "&amp;").replace("&amp;amp;", "&amp;")
            new_tags += f'    <string name="{key}">{val}</string>\n'
        
        final_strings = strings_content[:insert_idx] + new_tags + strings_content[insert_idx:]
        with open(STRINGS_FILE, "w") as f:
            f.write(final_strings)
        print("Strings extracted:", len(new_strings))
    else:
        print("Error: </resources> not found in strings.xml")
