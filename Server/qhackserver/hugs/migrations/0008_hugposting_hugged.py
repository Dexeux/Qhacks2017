# -*- coding: utf-8 -*-
# Generated by Django 1.10.5 on 2017-02-04 17:50
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('hugs', '0007_remove_hugposting_cached_pair'),
    ]

    operations = [
        migrations.AddField(
            model_name='hugposting',
            name='hugged',
            field=models.BooleanField(default=False),
        ),
    ]
