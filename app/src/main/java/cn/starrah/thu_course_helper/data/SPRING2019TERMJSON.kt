package cn.starrah.thu_course_helper.data

val SPRING2019TERMJSON = """
    {
      "schoolName": "清华大学",
      "beginYear": 2019,
      "type": "SPRING",
      "startDate": "2020-02-17",
      "normalWeekCount": 16,
      "examWeekCount": 2,
      "holidays": [
        {
          "date": "2020-04-06"
        },
        {
          "date": "2020-04-25"
        },
        {
          "date": "2020-04-26"
        },
        {
          "date": "2020-04-30"
        },
        {
          "date": "2020-05-01"
        },
        {
          "date": "2020-05-02",
          "to": "2020-05-04"
        },
        {
          "date": "2020-05-03",
          "to": "2020-05-05"
        },
        {
          "date": "2020-05-04",
          "to": "2020-05-09"
        },
        {
          "date": "2020-05-05"
        },
        {
          "date": "2020-05-09"
        }
      ],
      "timeRule": {
        "bigs": [
          {
            "smalls": [
              {
                "startTime": "08:00",
                "endTime": "08:45"
              },
              {
                "startTime": "08:50",
                "endTime": "09:35"
              }
            ]
          },
          {
            "smalls": [
              {
                "startTime": "09:50",
                "endTime": "10:35"
              },
              {
                "startTime": "10:40",
                "endTime": "11:25"
              },
              {
                "startTime": "11:30",
                "endTime": "12:15"
              }
            ]
          },
          {
            "smalls": [
              {
                "startTime": "13:30",
                "endTime": "14:15"
              },
              {
                "startTime": "14:20",
                "endTime": "15:05"
              }
            ]
          },
          {
            "smalls": [
              {
                "startTime": "15:20",
                "endTime": "16:05"
              },
              {
                "startTime": "16:10",
                "endTime": "16:55"
              }
            ]
          },
          {
            "smalls": [
              {
                "startTime": "17:05",
                "endTime": "17:50"
              },
              {
                "startTime": "17:55",
                "endTime": "18:40"
              }
            ]
          },
          {
            "smalls": [
              {
                "startTime": "19:20",
                "endTime": "20:05"
              },
              {
                "startTime": "20:10",
                "endTime": "20:55"
              },
              {
                "startTime": "21:00",
                "endTime": "21:45"
              }
            ]
          }
        ]
      }
    }
""".trimIndent()