from rest_framework import status
from rest_framework.response import Response
from rest_framework.authentication import TokenAuthentication
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.permissions import IsAuthenticated
from models import *
from geographiclib.geodesic import Geodesic
import json


# Create your views here.
@api_view(['POST'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def search(request):
    """
    Begin Searching for a hug
    """
    user = request.user
    request_data = json.loads(request.body)
    if request_data.get('latitude') and request_data.get('longitude'):
        print('getting type')
        print (type(request_data.get('latitude')))
        print (type(request_data.get('longitude')))
        if not hasattr(user, 'hugposting'):
            # Create hug posting
            posting = HugPosting(latitude=request_data.get('latitude'), longitude=request_data.get('longitude'),
                                 user_origin=user)
        else:
            posting = user.hugposting
            posting.latitude = request_data.get('latitude')
            posting.longitude = request_data.get('longitude')
        posting.save()
        if not posting.paired:
            total_posts = {}
            for hposts in HugPosting.objects.all():
                if not (hposts == user.hugposting):
                    if not hposts.paired:
                        distance = (posting.latitude - float(hposts.latitude))**2 + (posting.longitude - float(hposts.longitude))**2
                        total_posts[distance] = hposts
            if total_posts:
                # Got a pair, return coordinate
                posting.pair = total_posts[min(total_posts)]
                posting.save()
        if not posting.paired:
            # Show wait thing
            response_data = {'status': 'wait'}
            return Response(data=response_data, status=status.HTTP_200_OK)
        else:
            # Getting midpoint
            # Compute path from 1 to 2# Define the path from 1 to 2
            l = Geodesic.WGS84.InverseLine(posting.paired.latitude, posting.paired.longitude,
                                           posting.latitude, posting.longitude)
            m = l.Position(0.5 * l.s13)
            # lat = (posting.paired.latitude + posting.latitude)/2
            # lng = (posting.paired.longitude + posting.longitude)/2
            response_data = {'status': 'success',
                             'latitude': m['lat2'],
                             'longitude': m['lon2'],
                             }
            return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def hugging(request):
    """
    Begin Searching for a hug
    """
    user = request.user
    if not user.hugposting.hugged:
        user.hugposting.hugged = True
        user.hugposting.save()
    # delete both id hugged
    if user.hugposting.paired.hugged:
        # add score here
        if not hasattr(user, 'score'):
            score = Score(user_origin=user)
            score.save()
        if not hasattr(user.hugposting.paired.user_origin, 'score'):
            score = Score(user_origin=user.hugposting.paired.user_origin)
            score.save()
        score1 = user.score
        score1.hugs += 1
        score1.save()
        score2 = user.hugposting.paired.user_origin.score
        score2.hugs += 1
        score2.save()

        user.hugposting.paired.delete()
        user.hugposting.delete()
        response_data = {'status': 'success',
                         'message': 'hugged',
                         }
        return Response(data=response_data, status=status.HTTP_200_OK)
    else:
        response_data = {'status': 'wait',
                         }
        return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def hug_later(request):
    """
    Begin Searching for a hug
    """
    user = request.user
    if hasattr(user, 'hugposting'):
        if user.hugposting.paired:
            match = user.hugposting.paired
            match.ditched = True
            print('ditched')
            match.save()
        user.hugposting.delete()
    response_data = {'status': 'success',
                     'message': 'unhugged', }
    return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def get_status(request):
    """
    Begin Searching for a hug
    """
    user = request.user
    # Huggins still exists
    if hasattr(user, 'hugposting'):
        if user.hugposting.paired:
            response_data = {'status': 'wait', }
            return Response(data=response_data, status=status.HTTP_200_OK)
        elif user.hugposting.ditched:
            user.hugposting.delete()
            response_data = {'status': 'ditched', }
            return Response(data=response_data, status=status.HTTP_200_OK)

    if hasattr(user, 'hugposting'):
        user.hugposting.delete()
    response_data = {'status': 'success',
                     'message': 'failed', }
    return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
@authentication_classes((TokenAuthentication,))
@permission_classes((IsAuthenticated,))
def get_score(request):
    user = request.user
    if not hasattr(user, 'score'):
        score = Score(user_origin=user)
        score.save()
    response_data = {'status': 'success',
                     'score': str(user.score.hugs),
                     'user': user.username, }
    return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
def wipe_hugs(request):
    HugPosting.objects.all().delete()
    response_data = {'status': 'success',
                     }
    return Response(data=response_data, status=status.HTTP_200_OK)


@api_view(['GET'])
def hugs_list(request):
    data = []
    response_data = {}
    for hugs in HugPosting.objects.all():
        if hugs.paired:
            response_data[str(hugs.user_origin.username)] = str(hugs.paired.user_origin.username)
            response_data['lat'] = hugs.paired.latitude
            response_data['lng'] = hugs.paired.longitude
            data.append(response_data)
        else:
            response_data[str(hugs.user_origin.username)] = "Single"
            response_data['lat'] = hugs.latitude
            response_data['lng'] = hugs.longitude

    return Response(data=data, status=status.HTTP_200_OK)