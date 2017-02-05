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
    user_origin = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True)


class HugPosting(models.Model):
    """
    Used to keep track of the users hug pairing
    """
    latitude = models.DecimalField(default=1, max_digits=9, decimal_places=6)
    longitude = models.DecimalField(default=1, max_digits=9, decimal_places=6)
    date = models.DateTimeField(auto_now_add=True)
    pair = models.ForeignKey('self', related_name="pair_hug", blank=True, null=True)
    user_origin = models.OneToOneField(User, on_delete=models.CASCADE, primary_key=True)
    hugged = models.BooleanField(default=False)
    ditched = models.BooleanField(default=False)

    @property
    def paired(self):
        if self.pair:
            return self.pair
        else:
            return self.pair_hug.exclude(user_origin=self.user_origin).first()

