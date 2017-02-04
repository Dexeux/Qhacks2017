from rest_framework import status
from rest_framework.response import Response
from rest_framework.authtoken.models import Token
from django.contrib.auth import authenticate
from rest_framework.authentication import TokenAuthentication
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated
from django.contrib.auth import get_user_model
import json

User = get_user_model()


@api_view(['POST'])
def create_user(request):
    """
    Create a user
    """
    response = Response(data={'status': 'error'}, status=status.HTTP_200_OK)
    request_data = json.loads(request.body)
    #print(request_data)
    if request_data.get('username') and request_data.get('password') and request_data.get('email'):
        if User.objects.filter(username=request_data['username']).exists():
            response.data = {'status': 'error', 'message': 'Username is already taken'}
            return response
        elif len(request_data.get('password')) < 8:
            response.data = {'status': 'error', 'message': 'Password must be at least 8 characters'}
            return response
        elif "@" not in request_data.get('email') or "." not in request_data.get('email'):
            response.data = {'status': 'error', 'message': 'Please use a valid email'}
            return response
        else:
            print("to user")
            user = User.objects.create_user(username=request_data['username'],
                                            email=request_data['email'],
                                            password=request_data['password'])
            token = Token.objects.create(user=user)
            response.data = {
                    'status': 'success',
                    'token': token.key,
                }
            response.status_code = 200
            return response


@api_view(['POST'])
def authenticate_user(request):
    """
    Authenticate a user
    """
    response = Response(data={'status': 'error'}, status=status.HTTP_200_OK)
    request_data = json.loads(request.body)
    user = authenticate(username=request_data.get('username'), password=request_data.get('password'))
    if user is None:
        return response
    else:
        token, created = Token.objects.get_or_create(user=user)
        if not created:
            token.delete()
            token = Token.objects.create(user=user)
        response.data = {
            'status': 'success',
            'token': token.key,
        }
        response.status_code = 200
        return response


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def refresh_token(request):
    """
    Renew a token
    """
    user = request.user
    token, created = Token.objects.get_or_create(user=user)
    if not created:
        token.delete()
        token = Token.objects.create(user=user)
    response_data = {
        'status': 'success',
        'token': token.key,
    }
    return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def delete_token(request):
    """
    Delete a token
    """
    user = request.user
    token, created = Token.objects.get_or_create(user=user)
    token.delete()
    response_data = {
        'status': 'success'
    }
    return Response(data=response_data, status=status.HTTP_200_OK)
