from django.conf.urls import url
from . import views


urlpatterns = [
    url(r'^search', views.search),
    url(r'^hugging', views.hugging),
    url(r'^status', views.get_status),
    url(r'^hug_later', views.hug_later),
    url(r'^score', views.get_score),
    url(r'^wipe_hug', views.wipe_hugs),
    url(r'hugs_list',views.hugs_list),
]