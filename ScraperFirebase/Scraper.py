# -*- coding: utf-8 -*-
"""
Created on Mon Mar  5 12:10:43 2018

@author: David
"""
import datetime
import urllib.request
from bs4 import BeautifulSoup, Comment
import firebase_admin
from firebase_admin import credentials
from firebase.firebase import FirebaseApplication, FirebaseAuthentication
from firebase_admin import db


cred = credentials.Certificate("servicekey.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://college-pal-734fc.firebaseio.com/'
})

snapshot = []
# ======================================================================================================================
# get_firebase_users(email): takes the email for the db and extracts the data needed for the scraper to run based on
# each user unique ID(UID)in the database for writing to the location and their class code to select the right timetable.
# then calls scrape_timetable function for each user in the database.
# ======================================================================================================================


def get_firebase_users(email):
    dsn = 'https://college-pal-734fc.firebaseio.com'
    secret = 'aW7UonyfhprZVZrOn0bwKCUoGxiXupvTgGSSNUMh'
    authentication = FirebaseAuthentication(secret, email, True, True)
    firebase = FirebaseApplication(dsn, authentication)
    snapshot = firebase.get('users', None, params={'print': 'pretty'}, headers={'X_FANCY_HEADER': 'very fancy'})

    # key is the UID
    for key, val in snapshot.items():
        scrape_timetable(key, val['class'], 2)

# ======================================================================================================================
# write_schedule_to_user_db(path, data): Takes in the path for writing based on the UID of the user and the data.
#  The time is used asa key so the data contains the module name and the room name.
# ======================================================================================================================


def write_schedule_to_user_db(path, data):
    ref = db.reference('users' + path)
    ref.set({
        'module': data[0],
        'classroom': data[1]
    })


# ======================================================================================================================
# get_day_of_the_week(): Uses the datetime api to get the weekday number for inserting into the URI. Returns an int.
# ======================================================================================================================

def get_day_of_the_week():
    return datetime.datetime.today().weekday()


# ======================================================================================================================
# scrape_timetable(user, user_class, day): Takes in the UID, users class name and the day of the week. Scrapes the
#  timetable and then writes to the users database in this format Users/UID/Timetable/time/ module name and room name.
# ======================================================================================================================

def scrape_timetable(user, user_class, day):
    html_doc = urllib.request.urlopen('http://timetables.cit.ie:70/reporting/Individual;Student+Set;name;'+str(user_class)+'%0D%0A?weeks=&days='+str(day)+'&periods=1-40&height=100&width=100').read()
    soup = BeautifulSoup(html_doc, "html.parser")

    for comment in soup.findAll(text=lambda text: isinstance(text, Comment)):
        # from the comment made for each class box of information
        if comment in [' START OBJECT-CELL ']:
            object_cell = comment.parent.parent
            # get all data with background colour
            class_time = object_cell.find('td', bgcolor='#C0C0C0')
            # get all data assigned to the left
            class_name = object_cell.find('td', align='left')
            # get all the data with font color
            class_room = object_cell.find(color='#008000')
            # Write to the data to the user database
            data = [class_name.string, class_room.string]
            # Split the entries based on time
            write_schedule_to_user_db('/' + user + '/timetable/' + class_time.string + '/', data)
            print('Written to database -> ' + user)


def main():
    get_firebase_users('david.coughlan10@gmail.com')
    #exit()


if __name__ == '__main__':
    # 1. Input parameters

    debug = True

    main()

