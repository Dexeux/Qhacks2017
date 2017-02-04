from __future__ import unicode_literals

from django.db import models
from django.contrib.auth.models import User

# check for hug posting hasattr(user,'hugposting')

# Create your models here.


class Score(models.Model):
    """
    Used to keep track of the users hug score
    """
    hugs = models.IntegerField(default=0)
    rejects = models.IntegerField(default=0)
    user = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True)


class HugPosting(models.Model):
    """
    Used to keep track of the users hug pairing
    """
    latitude = models.IntegerField(default=0)
    longitude = models.IntegerField(default=0)
    pair = models.ForeignKey("self", blank=True, related_name="pair_hug")
    user_origin = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True)

