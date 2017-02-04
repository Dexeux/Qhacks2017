from django.conf.urls import url
from . import views


urlpatterns = [
    url(r'^register', views.create_user),
    url(r'^login', views.authenticate_user),
    url(r'^token', views.refresh_token),
    url(r'^logout', views.delete_token),
]